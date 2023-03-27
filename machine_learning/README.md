# Car Evaluation Machine Learning Model
This notebook presents the code for a machine learning model that predicts the buying price of a car given its maintenance level, number of doors, lug boot size, safety, and class value. 
The dataset used in this project is the Car Evaluation Data Set from UCI Machine Learning Repository.

The first step of the code is to load the dataset using pandas and convert categorical variables to numerical using one-hot encoding. Next, the dataset is split into training and testing sets using the train_test_split() method from the sklearn.model_selection module.

A RandomForestClassifier model from the sklearn.ensemble module is used to train the model on the training data. The model is then used to make a prediction on a new data point with the given parameters, and the predicted buying price is displayed.

The second part of the code involves the use of XGBoost to build a model to predict buying price based on the same set of features. The dataset is transformed using ordinal encoding and then split into training and testing sets. To account for imbalanced target classes, the compute_sample_weight() function from the sklearn.utils.class_weight module is used to compute sample weights for each class. A XGBClassifier model from the xgboost module is then trained on the training set with the sample_weight parameter set to the computed sample weights. The model is evaluated using the eval_set parameter, and the results are printed to the console.