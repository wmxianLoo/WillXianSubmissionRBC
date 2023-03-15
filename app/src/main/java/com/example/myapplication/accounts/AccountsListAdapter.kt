package com.example.myapplication.accounts

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.model.SingletonModel
import com.example.myapplication.R
import com.example.myapplication.transactions.TransactionsActivity
import com.rbc.rbcaccountlibrary.Account
import java.util.*

class AccountsListAdapter(private val context: Context, private val accountGroups: List<SingletonModel.AccountGroup>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //Class to contain header item
    class AccountsHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val header : TextView = itemView.findViewById(R.id.accounts_list_type_header)

        fun bind(group: SingletonModel.AccountGroup) {
            var title = group.title
            title = "${title[0].uppercase()}${title.substring(1).lowercase()}"
            //Had to do this because string.replace("_", " ") was not working
            if (title == "Credit_card") {
                title = "Credit card"
            }
            header.text = title
        }
    }

    //Class to contain account product item
    class AccountsProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val accountsName : TextView = itemView.findViewById(R.id.accounts_item_name)
        private val accountsNumber : TextView = itemView.findViewById(R.id.accounts_item_number)
        private val accountsBalance : TextView = itemView.findViewById(R.id.accounts_item_balance)

        fun bind(account: Account) {
            accountsName.text = account.name
            accountsNumber.text = account.number

            accountsBalance.text = SingletonModel.formatBalance(account.balance)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //Different layouts assigned to different view types
        return if (viewType ==  SingletonModel.HEADER_VIEW_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.accounts_list_sub_header, parent, false
            )
            AccountsHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.accounts_list_product_item, parent, false)
            AccountsProductViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        var count = 0
        accountGroups.forEach { group ->
            count += group.accounts.size + 1 // Add one to include account group header
        }
        return count
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //Different holders assigned to different view types
        when (holder.itemViewType) {
            SingletonModel.HEADER_VIEW_TYPE -> {
                val headerViewHolder = holder as AccountsHeaderViewHolder
                val groupIndex = getHeaderIndexForPosition(position)
                headerViewHolder.bind(accountGroups[groupIndex])
            }
            SingletonModel.PRODUCT_VIEW_TYPE -> {
                val productViewHolder = holder as AccountsProductViewHolder
                val account = getProductForPosition(position)
                productViewHolder.bind(account)

                // Make only the account item clickable
                productViewHolder.itemView.setOnClickListener {
                    val intent = Intent(context, TransactionsActivity::class.java)
                    intent.putExtra(SingletonModel.ACCOUNT_NAME, account.name)
                    intent.putExtra(SingletonModel.ACCOUNT_NUMBER, account.number)
                    intent.putExtra(SingletonModel.ACCOUNT_BALANCE,
                        SingletonModel.formatBalance(account.balance))
                    intent.putExtra(SingletonModel.ACCOUNT_TYPE, account.type.toString())
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var index = 0
        accountGroups.forEach { group ->
            if (position == index) {
                return SingletonModel.HEADER_VIEW_TYPE
            }
            index += group.accounts.size + 1 // +1 for header
            if (position < index) {
                return SingletonModel.PRODUCT_VIEW_TYPE
            }
        }
        throw IllegalArgumentException("Invalid position")
    }

    //Returns index of header; index of current account group
    private fun getHeaderIndexForPosition(position: Int): Int {
        //Reuse method to determine ViewType for onCreate and onBind usage
        var index = 0
        accountGroups.forEachIndexed { i, group ->
            if (position == index) {
                return i
            }
            index += group.accounts.size + 1 // +1 for header
            if (position < index) {
                return i
            }
        }
        throw IllegalArgumentException("Invalid position")
    }

    //Returns index of account within an account group
    private fun getProductIndexForPosition(position: Int): Int {
        val groupIndex = getHeaderIndexForPosition(position)
        val groupStartPosition = getHeaderStartPosition(groupIndex)
        return position - groupStartPosition - 1
    }

    //Returns Account for onBind to assign data to views
    private fun getProductForPosition(position: Int): Account {
        val groupIndex = getHeaderIndexForPosition(position)
        val itemIndex = getProductIndexForPosition(position)
        return accountGroups[groupIndex].accounts[itemIndex]
    }

    //Find header index for a group in the middle
    private fun getHeaderStartPosition(headerIndex: Int): Int {
        var index = 0
        accountGroups.take(headerIndex).forEach { group ->
            index += group.accounts.size + 1 // +1 for header
        }
        return index
    }
}