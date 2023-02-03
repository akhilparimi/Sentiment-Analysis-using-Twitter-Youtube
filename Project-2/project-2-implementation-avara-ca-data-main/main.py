from flask import Flask, render_template, request
from textblob import TextBlob
import numpy as np
import re
import pymongo
import datetime
import dateutil.parser

app = Flask(__name__)


@app.route('/')
def twitter_analysis():
    return render_template('index.html')


global positive1
positive1 = 0
global negative1
negative1 = 0
global neutral1
neutral1 = 0
global positive2
positive2 = 0
global negative2
negative2 = 0
global neutral2
neutral2 = 0


def clean_tweet(text_to_be_analyzed):
    return ' '.join(re.sub("(@[A-Za-z0-9]+)|([^0-9A-Za-z \t])|(#[A-Za-z0-9]+)", " ", str(text_to_be_analyzed)).split())


def analyze_sentiment(text_to_be_analyzed):
    global positive1, neutral1, negative1
    analysis = TextBlob(clean_tweet(text_to_be_analyzed))

    if analysis.sentiment.polarity > 0.00:
        positive1 += 1
    elif analysis.sentiment.polarity == 0.00:
        neutral1 += 1
    elif analysis.sentiment.polarity < 0.00:
        negative1 += 1


def analyze_yt_sentiment(text_to_be_analyzed):
    global positive2, neutral2, negative2
    analysis = TextBlob(clean_tweet(text_to_be_analyzed))

    if analysis.sentiment.polarity > 0.00:
        positive2 += 1
    elif analysis.sentiment.polarity == 0.00:
        neutral2 += 1
    elif analysis.sentiment.polarity < 0.00:
        negative2 += 1


@app.route('/result', methods=['POST', 'GET'])
def result():
    if request.method == 'POST':
        from_date = request.form['from_date']
        to_date = request.form['to_date']
        d1 = dateutil.parser.parse(from_date)
        d2 = dateutil.parser.parse(to_date)
        print(d1)
        print(d2)
        date1 = datetime.datetime.strptime(d1.strftime("%Y-%m-%d %H:%M:%S"),'%Y-%m-%d %H:%M:%S')
        date2 = datetime.datetime.strptime(d2.strftime("%Y-%m-%d %H:%M:%S"),'%Y-%m-%d %H:%M:%S')        
        client = pymongo.MongoClient("mongodb://localhost:27017/")
        # Database Name
        db = client["twitterDB"]
        # Collection Name
        col = db["tweets"]
        count = 0
        for tweet in col.find({"date": {"$gte": date1, "$lte": date2}}):
            count += 1
            np.array([analyze_sentiment(tweet)])
            print(count)
            if count == 50000:
                break 
        print(positive1)
        print(negative1)
        print(neutral1)
        values1 = [positive1, negative1, neutral1]
        #client.close()
        #client = pymongo.MongoClient("mongodb://localhost:27017/")
        db = client["YouTubeDB"]
        # Collection Name
        col = db["YTComments"]
        count = 0
        print("starting yt execution")
        print(date1)
        print(date2)
        for comment in col.find({"date": {"$gte": date1, "$lte": date2}}):
            count += 1
            np.array([analyze_yt_sentiment(comment)])
            print(count)
            if count == 50000:
                break
        print(positive2)
        print(negative2)
        print(neutral2)
        values2 = [positive2, negative2, neutral2]
        return render_template('index.html', analysis_values1=values1, analysis_values2=values2)


if __name__ == '__main__':
    app.run()

