
import org.apache.spark.sql.functions.{col, lit, udf, *}
import org.apache.spark.sql.{DataFrame, SparkSession}


/**
 * This class outlines the implementation of a pipeline to process membership applications submitted by users on an hourly basis.
 * The pipeline will perform data cleaning, validity checks, and membership ID creation for successful applications.
 *
 *Pipeline Architecture
 * The pipeline will be divided into the following stages:
 * Data Ingestion:
 *     This stage will read in the application data from a folder on an hourly basis.
 * Data Cleaning and Validation:
 *     This stage will clean and validate the data according to the requirements as follows:
 *     1. Split name into first_name and last_name
 *     2. Format birthday field into YYYYMMDD
 *     3. Remove any rows which do not have a name value (treat this as unsuccessful applications)
 *     4. Create a new field named above_18 based on the applicant's birthdate
 *     5. Check if the mobile number is 8 digits
 *     6. Check if the applicant is over 18 years old as of 1 Jan 2022
 *     7. Check if the applicant's email can only have top level domain of .com and .net
 *     Rows that do not meet the criteria will be dropped.
 * Membership ID Creation:
 *      The membership ID will be created for successful applications as follows:
 *      1. Concatenate the applicant's last name and a SHA256 hash of the applicant's birthday.
 *      2. Truncate the hash to the first 5 digits.
 *      3.Concatenate the last name and truncated hash to create the membership ID.
 *Output:
 *      This stage will write successful and unsuccessful applications to separate folders.
 *
 * Assumptions:
 *  1. Input File is written in folder with structure as /base_path/{YYYYMMDD}/{HH}
 *    e.g. for 25 Mar 2023 16 hour file will be written in path /base_path/20230325/14/
 *  2. If a name contains more than 2 words excluding salutations and Qualification prefixes and suffixes,
 *      then Last Name will be the last word, rest words will be First Name
 *
 *
 */
object PipelineMain {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val spark = getSparkSession()
    val namedArgs = getNamedArgs(args)
    // Read cli arguments
    val inputPath = namedArgs("input_path")
    val successfulOutputPath = namedArgs("successful_output_path")
    val unSuccessfulOutputPath = namedArgs("unsuccessful_output_path")
    val batchTime = namedArgs("batchtime")
    val batchDay = batchTime.substring(0, 8)
    val batchHour = batchTime.substring(8, 10)
    //create input path for the day & hour as per batchtime
    val inputPathToRead = s"$inputPath/$batchDay/$batchHour"
    // Data Ingestion: Read input file
    val df = spark.read.option("header", "true").csv(inputPathToRead)
    //Data Cleaning & Validation
    val cleanedDF = cleanAndFormatData(df)
    val successfulDF = getSuccessfulApplicants(cleanedDF)
    val unSuccesfulDF = getUnsuccessfulApplicants(cleanedDF, successfulDF)
//    Membership ID Creation:
    val membershipIdDF = createMembershipIds(successfulDF).drop("hashed_birthday", "truncated_hash")

// Write Successful Applicants output file
    membershipIdDF.repartition(1).write.mode(SaveMode.Overwrite)
      .option("header", "true").csv(s"$successfulOutputPath/$batchDay/$batchHour")

    // Write Unsuccessful Applicants output file
    unSuccesfulDF.repartition(1).write.mode(SaveMode.Overwrite)
      .option("header", "true").csv(s"$unSuccessfulOutputPath/$batchDay/$batchHour")


  }

  def getSparkSession() = {

    val spark = SparkSession.builder()
      .appName("Members Pipeline")
      .getOrCreate()
    spark

  }

  /**
   * Function to parse the command Line arguments passed.
   * Arguments will be in the form key=value
   * @param args
   * @return Map argument as key and its passed value.
   */
  def getNamedArgs(args: Array[String]): Map[String, String] = {
    args.filter(line => line.contains("=")) //take only named arguments
      .map(x => (x.substring(0, x.indexOf("=")), x.substring(x.indexOf("=") + 1))) //split into key values
      .toMap //convert to a map
  }

  /**
   * Extract First and Last Names from names column.
   * Assumption:
   *  If a name contains more than 2 words excluding salutations and Qualification prefixes and suffixes,
   *     then Last Name will be the last word, rest words will be First Name
   *
   * @param df
   * @return
   */
  def splitFirstAndLastNames(df: DataFrame) = {

    val prefixes = List("Mr.", "Mrs.", "Ms.", "Dr.", "Prof.", "Rev.")
    val suffixes = List("Jr.", "Sr.", "PhD", "MD", "DDS", "DVM")


    // Split name into words and remove prefixes/suffixes
    val wordsDF = df
      .withColumn("words", split(col("name"), "\\s+"))
      .withColumn("words_no_prefix",    //Remove Salutation prefix
        array_except(col("words"), array(prefixes.map(lit(_)): _*))
      )
      .withColumn("words_no_prefix_suffix",     //Remove Qualification suffix
        array_except(col("words_no_prefix"), array(suffixes.map(lit(_)): _*)).as("words_no_prefix_suffix")
      )

    // Extract first name and last name
    val extractedNamesDF = wordsDF
      .withColumn("first_name", col("words_no_prefix_suffix")(0))
      .withColumn("last_name", col("words_no_prefix_suffix")(size(col("words_no_prefix_suffix")) - 1))

    extractedNamesDF


  }

  /**
   *
   * Format birthday field into YYYYMMDD
   * Create a new field named above_18 based on the applicant's birthdate
   *
   * @param df
   * @return
   */
  def getAge(df: DataFrame) = {
    // Extract year from string date
    val yearDF = df
      .withColumn("date", coalesce(
        to_date(col("date_of_birth"), "yyyy/MM/dd"),
        to_date(col("date_of_birth"), "yyyy-MM-dd"),
        to_date(col("date_of_birth"), "MMM dd, yyyy"),
        to_date(col("date_of_birth"), "dd MMM, yyyy"),
        to_date(col("date_of_birth"), "MM-dd-yyyy"),
        to_date(col("date_of_birth"), "dd-MM-yyyy"),
        to_date(col("date_of_birth"), "MM/dd/yyyy"),
        to_date(col("date_of_birth"), "dd/MM/yyyy"),
        to_date(col("date_of_birth"), "dd-MMM-yyyy"),
        to_date(col("date_of_birth"), "yyyy.MM.dd"),
        to_date(col("date_of_birth"), "MM.dd.yyyy"),
        to_date(col("date_of_birth"), "dd.MM.yyyy"),
      ))
      .withColumn("year", year(col("date")))
      .withColumn("age", year(to_date(lit("2022-01-01"),"yyyy-MM-dd")) - col("year") - lit(1))
      .withColumn("date_of_birth", date_format(col("date"),"yyyyMMdd"))
      .withColumn("above_18", col("age") >= 18)

    yearDF
  }

  /**
   * Data Cleaning and Formatting:
   * This stage will clean and validate the data according to the requirements as follows:
   *     1. Split name into first_name and last_name
   *     2. Format birthday field into YYYYMMDD
   *     3. Create a new field named above_18 based on the applicant's birthdate
   *
   * @param df
   * @return
   */

  def cleanAndFormatData(df: DataFrame): DataFrame = {
    // Split name into first_name and last_name
    val splitNameDf = splitFirstAndLastNames(df).drop("words", "words_no_prefix_suffix", "words_no_prefix")

    // Format birthday field into YYYYMMDD
    val formatBirthdayDf = getAge(splitNameDf).drop("date", "year", "age")

    formatBirthdayDf

  }

  /**
   *
   * An application is successful if:
   *
   *  Application mobile number is 8 digits
   *  Applicant is over 18 years old as of 1 Jan 2022
   *  Applicant's email can only have top level domain of .com and .net
   *
   * @param df
   * @return
   */

  def getSuccessfulApplicants(df: DataFrame): DataFrame = {
    // Remove rows without a name value
    val dropNamelessDf = df.filter(col("name").isNotNull)

    // Check if mobile number is 8 digits
    val validMobileDf = dropNamelessDf.filter(col("mobile_no").rlike("\\d{8}"))

    // Check if applicant is over 18 years old as of 1 Jan 2022
    val validBirthdateDf = validMobileDf.filter(col("above_18"))

    // Check if email has top level domain of .com or .net
    val validEmailDf = validBirthdateDf.filter(col("email").rlike("^[a-zA-Z0-9_.+-]+@(?:[a-zA-Z0-9-]+\\.)+(?:com|net)$"))

    validEmailDf

  }

  /**
   * Other than successful applications, all other are unsuccessful
   *
   * @param cleanedDF
   * @param successfulDF
   * @return
   */

  def getUnsuccessfulApplicants(cleanedDF: DataFrame, successfulDF): DataFrame = {

    cleanedDF.except(successfulDF)

  }


  /**
   * Membership ID Creation:
   * The membership ID will be created for successful applications as follows:
   *      1. Concatenate the applicant's last name and a SHA256 hash of the applicant's birthday.
   *         2. Truncate the hash to the first 5 digits.
   *         3.Concatenate the last name and truncated hash to create the membership ID.
   *
   * @param df
   * @return
   */
  def createMembershipIds(df: DataFrame): DataFrame = {
    val hashedBirthdayDf = df.withColumn("hashed_birthday", sha2(col("date_of_birth"), 256))
      .withColumn("truncated_hash", substring(col("hashed_birthday"), 1, 5))

    val membershipIdDf = hashedBirthdayDf.withColumn("membership_id", concat(col("last_name"), lit("_"), col("truncated_hash")))

    membershipIdDf
  }
}
