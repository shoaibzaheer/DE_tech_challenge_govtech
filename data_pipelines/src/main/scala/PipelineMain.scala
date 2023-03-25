
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
 */
object PipelineMain {
  def main(args: Array[String]): Unit = {

    // Initialize SparkSession
    val spark = getSparkSession()
    val namedArgs = getNamedArgs(args)
    val inputPath = namedArgs("input_path")


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

}
