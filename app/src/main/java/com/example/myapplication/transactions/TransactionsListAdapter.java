package com.example.myapplication.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.model.SingletonModel;
import com.example.myapplication.R;
import com.rbc.rbcaccountlibrary.Transaction;
import java.util.List;

public class TransactionsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<SingletonModel.TransactionGroup> mTransactionGroups;

        //Class to contain header item
        private static class TransactionsHeaderViewHolder extends RecyclerView.ViewHolder {

                TextView header;

                TransactionsHeaderViewHolder(View itemView) {
                        super(itemView);
                        header = itemView.findViewById(R.id.transactions_list_date_header);
                }

                void bind(SingletonModel.TransactionGroup group) {
                        String title = group.getTitle();
                        title = title.replace(".","");
                        header.setText(title);
                }
        }

        //Class to contain transaction item
        private static class TransactionsItemViewHolder extends RecyclerView.ViewHolder {
                TextView transactionDescription;
                TextView transactionBalance;

                TransactionsItemViewHolder(View itemView) {
                        super(itemView);
                        transactionDescription = itemView.findViewById(
                                R.id.transactions_item_description);
                        transactionBalance = itemView.findViewById(
                                R.id.transactions_item_balance);
                }

                void bind(Transaction transaction) {
                        transactionDescription.setText(transaction.getDescription());
                        transactionBalance.setText(
                                SingletonModel.formatBalance(transaction.getAmount()));
                }
        }

        public TransactionsListAdapter(List<SingletonModel.TransactionGroup> transactionGroups) {
                mTransactionGroups = transactionGroups;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                //Different layouts assigned to different view types
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                if (viewType == SingletonModel.HEADER_VIEW_TYPE) {
                        View view = inflater.inflate(R.layout.transactions_list_sub_header,
                                parent, false);
                        return new TransactionsHeaderViewHolder(view);
                } else {
                        View view = inflater.inflate(R.layout.transactions_list_item,
                                parent, false);
                        return new TransactionsItemViewHolder(view);
                }
        }

        @Override
        public int getItemCount() {
                int count = 0;
                for (SingletonModel.TransactionGroup group : mTransactionGroups) {
                        count += group.getTransactions().size() + 1; // Add one to include header
                }
                return count;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                //Different holders assigned to different view types
                switch (holder.getItemViewType()) {
                        case SingletonModel.HEADER_VIEW_TYPE:
                                TransactionsHeaderViewHolder headerViewHolder =
                                        (TransactionsHeaderViewHolder) holder;
                                int groupIndex = getHeaderIndexForPosition(position);
                                headerViewHolder.bind(mTransactionGroups.get(groupIndex));
                                break;
                        case SingletonModel.PRODUCT_VIEW_TYPE:
                                TransactionsItemViewHolder productViewHolder =
                                        (TransactionsItemViewHolder) holder;
                                Transaction transaction = getItemForPosition(position);
                                productViewHolder.bind(transaction);
                                break;
                }
        }

        @Override
        public int getItemViewType(int position) {
                int index = 0;

                for (SingletonModel.TransactionGroup group : mTransactionGroups) {
                        if (position == index) {
                                return SingletonModel.HEADER_VIEW_TYPE;
                        }

                        index += group.getTransactions().size() + 1;
                        if (position < index) {
                                return SingletonModel.PRODUCT_VIEW_TYPE;
                        }
                }
                throw new IllegalArgumentException("Invalid position");
        }

        //Returns index of header; index of current transaction group
        private int getHeaderIndexForPosition(int position) {
                int index = 0;
                for (int i = 0; i < mTransactionGroups.size(); i++) {
                        SingletonModel.TransactionGroup group = mTransactionGroups.get(i);
                        if (position == index) {
                                return i;
                        }
                        index += group.getTransactions().size() + 1; // +1 for header
                        if (position < index) {
                                return i;
                        }
                }
                throw new IllegalArgumentException("Invalid position");
        }

        //Returns index of account within an transaction group
        private int getItemIndexForPosition(int position) {
                int groupIndex = getHeaderIndexForPosition(position);
                int groupStartPosition = getHeaderStartPosition(groupIndex);
                return position - groupStartPosition - 1;
        }

        //Returns Transaction for onBind to assign data to views
        private Transaction getItemForPosition(int position) {
                int groupIndex = getHeaderIndexForPosition(position);
                int itemIndex = getItemIndexForPosition(position);
                return mTransactionGroups.get(groupIndex).getTransactions().get(itemIndex);
        }

        //Find header index for a group in the middle
        private int getHeaderStartPosition(int headerIndex) {
                int index = 0;
                for (int i = 0; i < headerIndex; i++) {
                        SingletonModel.TransactionGroup group = mTransactionGroups.get(i);
                        index += group.getTransactions().size() + 1; // +1 for header
                }
                return index;
        }
}
