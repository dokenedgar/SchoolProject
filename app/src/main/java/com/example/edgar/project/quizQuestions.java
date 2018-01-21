package com.example.edgar.project;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Edgar on 3/4/2016.
 */
public class quizQuestions implements Parcelable {


        public quizQuestions() {

        }

        String qid;
        String question;
        String imgUrl;
        String optionA;
        String optionB;
        String optionC;
        String optionD;
        String correctAnswer;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getOptionA() {
            return optionA;
        }

        public void setOptionA(String optionA) {
            this.optionA = optionA;
        }

        public String getOptionB() {
            return optionB;
        }

        public void setOptionB(String optionB) {
            this.optionB = optionB;
        }

        public String getOptionC() {
            return optionC;
        }

        public void setOptionC(String optionC) {
            this.optionC = optionC;
        }

        public String getOptionD() {
            return optionD;
        }

        public void setOptionD(String optionD) {
            this.optionD = optionD;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }


        public String getQid() {
            return qid;
        }

        public void setQid(String qid) {
            this.qid = qid;
        }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.qid);
        dest.writeString(this.question);
        dest.writeString(this.optionA);
        dest.writeString(this.optionB);
        dest.writeString(this.optionC);
        dest.writeString(this.optionD);
        dest.writeString(this.correctAnswer);
    }

    protected quizQuestions(Parcel in) {
        this.qid = in.readString();
        this.question = in.readString();
        this.optionA = in.readString();
        this.optionB = in.readString();
        this.optionC = in.readString();
        this.optionD = in.readString();
        this.correctAnswer = in.readString();
    }

    public static final Parcelable.Creator<quizQuestions> CREATOR = new Parcelable.Creator<quizQuestions>() {
        public quizQuestions createFromParcel(Parcel source) {
            return new quizQuestions(source);
        }

        public quizQuestions[] newArray(int size) {
            return new quizQuestions[size];
        }
    };
}
