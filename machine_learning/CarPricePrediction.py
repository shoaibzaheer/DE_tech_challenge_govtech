# Databricks notebook source
# Using the dataset from https://archive.ics.uci.edu/ml/datasets/Car+Evaluation, create a machine learning model to predict the buying price given the following parameters:

# Maintenance = High
# Number of doors = 4
# Lug Boot Size = Big
# Safety = High
# Class Value = Good

# COMMAND ----------

import pandas as pd

# Load the dataset
url = 'https://archive.ics.uci.edu/ml/machine-learning-databases/car/car.data'
col_names = ['buying', 'maint', 'doors', 'persons', 'lug_boot', 'safety', 'class']
df = pd.read_csv(url, header=None, names=col_names)

# Convert categorical variables to numerical using one-hot encoding
df = pd.get_dummies(df, columns=['maint', 'doors', 'lug_boot', 'safety', 'class'])


# COMMAND ----------

from sklearn.model_selection import train_test_split

X = df.drop('class', axis=1)
y = df['class'].apply(lambda x: 1 if x == 'good' else 0)

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)

# COMMAND ----------

# from sklearn.model_selection import train_test_split

# X = df_encoded.drop('class', axis=1)
# y = df_encoded['class'].apply(lambda x: 1 if x == 'good' else 0)

# X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=42)



# train, test = df_encoded.randomSplit([0.7, 0.3], seed=42)


# COMMAND ----------

from sklearn.ensemble import RandomForestClassifier

rfc = RandomForestClassifier(n_estimators=100, random_state=42)
rfc.fit(X_train, y_train)

# COMMAND ----------

# Create a new DataFrame with the given parameters
X_new = pd.DataFrame(columns=col_names[:-1])
X_new.loc[0] = ['high', 'high', '4', 'more', 'big', 'high']

# Convert categorical variables to numerical using one-hot encoding
X_new = pd.get_dummies(X_new, columns=['buying', 'maint', 'doors', 'persons', 'lug_boot', 'safety'])

# Make a prediction
predicted_proba = rfc.predict_proba(X_new)[0][1]
max_price = df[df['class'] == 'good']['buying_vhigh'].max()
predicted_price = predicted_proba * max_price

print(f"Predicted probability of the class being 'good': {predicted_proba:.2f}")
print(f"Predicted buying price: {predicted_price:.2f}")


# COMMAND ----------

# MAGIC %md # code

# COMMAND ----------

!pip install xgboost

# COMMAND ----------

import pandas as pd
from sklearn.preprocessing import OrdinalEncoder
from sklearn.model_selection import train_test_split
from sklearn.utils.class_weight import compute_sample_weight
import xgboost as xgb

# Load the dataset
url = 'https://archive.ics.uci.edu/ml/machine-learning-databases/car/car.data'
col_names = ['buying', 'maint', 'doors', 'persons', 'lug_boot', 'safety', 'class']
data_raw = pd.read_csv(url, header=None, names=col_names)



# COMMAND ----------


# taking only those columns mentioned as per the requirements 
cols = ['buying', 'maint', 'doors', 'lug_boot', 'safety']
label = 'buying'
feats = [x for x in data_raw.columns if x!=label]
seed = 1234

# performing ordinal encoding 
buying_price_category = ['low', 'med', 'high', 'vhigh']
maint_cost_category = ['low', 'med', 'high', 'vhigh']
doors_category = ['2', '3', '4', '5more']
person_capacity_category = ['2', '4', 'more']
lug_boot_category = ['small', 'med', 'big']
safety_category = ['low', 'med', 'high']
class_category = ['unacc', 'acc', 'good', 'vgood']



all_categories = [ buying_price_category, maint_cost_category,doors_category, person_capacity_category,
                  lug_boot_category,safety_category, class_category]
oe = OrdinalEncoder(categories= all_categories)

# transforming data
data_trans = oe.fit_transform(data_raw)
data_trans = pd.DataFrame(data_trans, index=data_raw.index, columns=data_raw.columns)

# # # splitting data in to train and test datasets
X_train, X_test, y_train, y_test = train_test_split(data_trans[feats], data_trans[label],
                                                    test_size = 0.3, random_state=seed)

## ---------- XGBoost model  ----------
## treat unbalanced target classes

# balancing  class weights
sample_weights = compute_sample_weight(
    class_weight='balanced',
    y=y_train)

# declaring and fitting xgb classifier
xgb_clf = xgb.XGBClassifier(objective='multi:softmax', 
                            num_class=4, 
                            gamma=0, # default gamma value
                            learning_rate=0.1,
                            early_stopping_rounds=10,
                            eval_metric=['merror','mlogloss'],
                            seed=seed)
xgb_clf.fit(X_train, 
            y_train,
            verbose=0, # set to 1 to see xgb training round intermediate results
            sample_weight=sample_weights, # class weights to combat unbalanced 'target'
            eval_set=[(X_train, y_train), (X_test, y_test)])

y_pred = xgb_clf.predict(X_test)


# COMMAND ----------

# Given variables
# Maintenance = High
# Number of doors = 4
# Lug Boot Size = Big
# Safety = High
# Class Value = Good
d = {'maint': ['high'], 'doors': ['4'], 'persons':[None], 'lug_boot':['big'], 'safety':['high'], 'class':['good']}
d_trans = {'maint': [2.0], 'doors': [1.0], 'persons':[1.0], 'lug_boot':[2.0], 'safety':[2.0], 'class':[2.0]}
df_given = pd.DataFrame(d_trans)
pred = xgb_clf.predict(df_given)
pred_prob = xgb_clf.predict_proba(df_given)

print("Predicted class is : '{}'; with correspnding prediction score:'{}''".format(buying_price_category[int(pred)], pred_prob[0][int(pred)]))






# COMMAND ----------


