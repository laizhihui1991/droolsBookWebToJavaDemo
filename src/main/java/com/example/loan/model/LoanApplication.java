package com.example.loan.model;

public class LoanApplication {

    private String applicantName;

    private int age;

    private int workYears;

    private double annualIncome;

    private boolean goodCredit;

    private double debtRatio;

    // 以下两个字段由规则计算产生
    private int creditScore;

    private String creditLevel;

    public LoanApplication() {
    }

    public LoanApplication(String applicantName,
                           int age,
                           int workYears,
                           double annualIncome,
                           boolean goodCredit,
                           double debtRatio) {
        this.applicantName = applicantName;
        this.age = age;
        this.workYears = workYears;
        this.annualIncome = annualIncome;
        this.goodCredit = goodCredit;
        this.debtRatio = debtRatio;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getWorkYears() {
        return workYears;
    }

    public void setWorkYears(int workYears) {
        this.workYears = workYears;
    }

    public double getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(double annualIncome) {
        this.annualIncome = annualIncome;
    }

    public boolean isGoodCredit() {
        return goodCredit;
    }

    public void setGoodCredit(boolean goodCredit) {
        this.goodCredit = goodCredit;
    }

    public double getDebtRatio() {
        return debtRatio;
    }

    public void setDebtRatio(double debtRatio) {
        this.debtRatio = debtRatio;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public String getCreditLevel() {
        return creditLevel;
    }

    public void setCreditLevel(String creditLevel) {
        this.creditLevel = creditLevel;
    }
}