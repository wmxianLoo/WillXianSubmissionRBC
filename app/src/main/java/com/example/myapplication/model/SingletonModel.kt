package com.example.myapplication.model

import android.util.Log
import com.rbc.rbcaccountlibrary.Account
import com.rbc.rbcaccountlibrary.Transaction
import java.text.NumberFormat
import java.util.*

object SingletonModel {
    data class AccountGroup(val title: String, val accounts: List<Account>)
    data class TransactionGroup(val title: String, val transactions: List<Transaction>)


    const val ACCOUNT_NAME = "accountName"
    const val ACCOUNT_NUMBER = "accountNumber"
    const val ACCOUNT_BALANCE = "accountBalance"
    const val ACCOUNT_TYPE = "accountType"
    const val HEADER_VIEW_TYPE = 0
    const val PRODUCT_VIEW_TYPE = 1

    //Format balance string
    @JvmStatic
    fun formatBalance(balance: String) : String {
        //Assuming no balance would exceed MAX_INT or MIN_INT
        //Add commas to number
        val number = balance.toDouble()
        val numberFormat = NumberFormat.getNumberInstance(Locale.CANADA)
        var newBalance = numberFormat.format(number)
        //Add dollar sign
        newBalance = if (newBalance[0] == '-') {
            newBalance[0] + "$" + newBalance.substring(1)
        } else {
            "$" + newBalance.substring(0)
        }
        //Add 0 to the end if missing
        for (i in newBalance.indices) {
            if (newBalance[i] == '.') {
                if (i + 1 == newBalance.length - 1) {
                    newBalance += "0"
                }
            }
        }

        return newBalance;
    }
}