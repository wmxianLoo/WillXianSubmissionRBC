package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.accounts.AccountsListAdapter
import com.example.myapplication.model.SingletonModel
import com.rbc.rbcaccountlibrary.Account
import com.rbc.rbcaccountlibrary.AccountProvider
import com.rbc.rbcaccountlibrary.AccountType

class MainActivity : AppCompatActivity() {

    var groups = mutableListOf<SingletonModel.AccountGroup>()
    private val accountTypeMap = mutableMapOf<String, MutableList<Account>>()
    lateinit var accounts : List<Account>

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: AccountsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_accounts)

        recyclerView = findViewById(R.id.accounts_list)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        //Get Accounts
        accounts = AccountProvider.getAccountsList()

        //Create mapping of account types to their lists
        enumValues<AccountType>().forEach {
            accountTypeMap[it.name] = mutableListOf()
        }
        //Group account by type
        accounts.forEach {
            accountTypeMap[it.type.name]?.add(it)
        }

        //Assign accounts to adapter data model
        accountTypeMap.forEach {
            if (!it.value.isEmpty()) {
                groups.add(SingletonModel.AccountGroup(it.key, it.value))
            }
        }

        adapter = AccountsListAdapter(this, groups as List<SingletonModel.AccountGroup>)
        recyclerView.adapter = adapter
    }

}