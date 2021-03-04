package com.antoinegrandin.bankaccount

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_NAME = "BankDb"
const val TABLE_NAME = "AccountsTable"
const val COL_AccountName = "accountName"
const val COL_Amount = "amount"
const val COL_Iban = "iban"
const val COL_Currency = "currency"
const val COL_ID = "id"

class DataBaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AccountName + " VARCHAR(256), " +
                COL_Amount + " VARCHAR(256), " +
                COL_Iban + " VARCHAR(256), " +
                COL_Currency + " VARCHAR(256))"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun insertData(account : Account){
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_AccountName, account.accountName)
        cv.put(COL_Amount, account.amount)
        cv.put(COL_Iban, account.iban)
        cv.put(COL_Currency, account.currency)
        db.insert(TABLE_NAME, null, cv)
    }

    fun readData() : MutableList<Account>{
        val list : MutableList<Account> = ArrayList()

        val db = this.readableDatabase
        val query = "Select * from $TABLE_NAME"
        val result = db.rawQuery(query, null)

        if(result.moveToFirst()){
            do{
                val account = Account()
                account.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                account.accountName = result.getString(result.getColumnIndex(COL_AccountName))
                account.amount = result.getString(result.getColumnIndex(COL_Amount))
                account.iban = result.getString(result.getColumnIndex(COL_Iban))
                account.currency = result.getString(result.getColumnIndex(COL_Currency))
                list.add(account)
            }while(result.moveToNext())
        }

        result.close()
        db.close()
        return list
    }

    fun cleanData(){
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME;")
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name='$TABLE_NAME';")
        db.execSQL("DROP TABLE $TABLE_NAME")

        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AccountName + " VARCHAR(256), " +
                COL_Amount + " VARCHAR(256), " +
                COL_Iban + " VARCHAR(256), " +
                COL_Currency + " VARCHAR(256))"
        db.execSQL(createTable)
    }
}