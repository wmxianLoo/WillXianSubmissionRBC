package com.example.myapplication.transactions;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.model.SingletonModel;
import com.rbc.rbcaccountlibrary.AccountProvider;
import com.rbc.rbcaccountlibrary.AccountType;
import com.rbc.rbcaccountlibrary.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionsActivity extends Activity {
    private TextView loadingIndicator;
    private TextView emptyList;
    private RecyclerView recyclerView;
    private TransactionsListAdapter adapter;
    private List<Transaction> creditCardTransactions;
    private final TransactionsTask[] tasks = new TransactionsTask[4];
    private List<SingletonModel.TransactionGroup> groups = new ArrayList<>();
    private String accountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_transactions);

        TextView accountName = findViewById(R.id.accounts_name);
        TextView accountNumber = findViewById(R.id.accounts_number);
        TextView accountBalance = findViewById(R.id.accounts_balance);
        loadingIndicator = findViewById(R.id.loading_indicator);
        emptyList = findViewById(R.id.empty_list);
        ImageButton back = findViewById(R.id.backToMain);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        String accountNumberString = intent.getStringExtra(SingletonModel.ACCOUNT_NUMBER);
        accountName.setText(intent.getStringExtra(SingletonModel.ACCOUNT_NAME));
        accountNumber.setText(accountNumberString);
        accountBalance.setText(intent.getStringExtra(SingletonModel.ACCOUNT_BALANCE));
        accountType = intent.getStringExtra(SingletonModel.ACCOUNT_TYPE);

        recyclerView = findViewById(R.id.transactions_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = new TransactionsTask();
            tasks[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, accountNumberString);
        }
    }

    @Override
    public void onDestroy() {
        //Cancel async task if activity is destroyed or screen is rotated
        for (TransactionsTask task : tasks) {
            if (task.getStatus() == AsyncTask.Status.PENDING ||
                    task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
        }
        super.onDestroy();
    }

    //Sort transactions in descending order (newest first)
    private void sortTransactions(List<Transaction> transactions) {
        transactions.sort((transaction1, transaction2) ->
                transaction2.getDate().getTime().compareTo(transaction1.getDate().getTime()));
    }

    //Group transactions by date for display
    private void groupTransactions(List<Transaction> transactions) {
        //Date format: EEE, MMMM d, yyyy
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("EEE, MMMM d, yyyy", Locale.CANADA);
        String prevFormattedDate = "";
        String currentFormattedDate;
        List<Transaction> transactionList = new ArrayList<>();
        for (Transaction transaction : transactions) {
            currentFormattedDate = dateFormat.format(
                    transaction.getDate().getTime());

            if (!currentFormattedDate.equals(prevFormattedDate)) { //New date group
                if (!prevFormattedDate.equals("")) {
                    groups.add(new SingletonModel.TransactionGroup(
                            prevFormattedDate, transactionList));
                }
                transactionList = new ArrayList<>();
                transactionList.add(transaction);
            } else {
                transactionList.add(transaction);
            }

            prevFormattedDate = currentFormattedDate;
        }
        groups.add(new SingletonModel.TransactionGroup(
                prevFormattedDate, transactionList));
    }

    //Async Task to get transactions
    class TransactionsTask extends AsyncTask<String, Void, List<Transaction>> {

        @Override
        protected List<Transaction> doInBackground(String... accountNumber) {
            List<Transaction> transactions;
            try {
                transactions = AccountProvider.INSTANCE.getTransactions(accountNumber[0]);
            } catch (Exception e) {
                return null;
            }
            if (accountType.equals(AccountType.CREDIT_CARD.toString())) {
                try {
                    creditCardTransactions = AccountProvider.INSTANCE
                            .getAdditionalCreditCardTransactions(accountNumber[0]);
                } catch (Exception e) {
                    return null;
                }
            }
            if (creditCardTransactions != null) {
                transactions.addAll(creditCardTransactions);
                creditCardTransactions.clear();
            }
            return transactions;
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            if (transactions != null && adapter == null) {
                loadingIndicator.setVisibility(View.GONE);
                if (transactions.isEmpty()) {
                    emptyList.setVisibility(View.VISIBLE);
                    return;
                }
                sortTransactions(transactions);
                groupTransactions(transactions);

                adapter = new TransactionsListAdapter(groups);
                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
