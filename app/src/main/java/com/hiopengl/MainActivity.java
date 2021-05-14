package com.hiopengl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.hiopengl.examples.ExampleCategory;
import com.hiopengl.examples.ExampleFactory;
import com.hiopengl.examples.ExampleInfo;
import com.hiopengl.examples.ExampleItem;

public class MainActivity extends Activity {

    private ListView mListView;
    private ExampleItemAdapter mExampleAdapter;
    private ExampleCategory mAllCategory;
    private ExampleCategory mParentCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = findViewById(R.id.list_view);
        mExampleAdapter = new ExampleItemAdapter(this, R.layout.list_view_item);
        mAllCategory = ExampleFactory.getAllExamples();
        mParentCategory = mAllCategory;
        mExampleAdapter.addAll(mParentCategory.getItems());
        mListView.setAdapter(mExampleAdapter);
    }

    @Override
    public void onBackPressed() {
        ExampleCategory parentCategory = ExampleFactory.getParentCategory(mAllCategory, mParentCategory);
        if (parentCategory != null) {
            mParentCategory = parentCategory;
            mExampleAdapter.clear();
            mExampleAdapter.addAll(parentCategory.getItems());
            mExampleAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    private class ExampleItemAdapter extends ArrayAdapter<ExampleItem> {

        private LayoutInflater mInflater;

        public ExampleItemAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_view_item, null);
                holder = new ViewHolder();

                holder.item = convertView.findViewById(R.id.item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            final ExampleItem exampleItem = getItem(position);
            holder.item.setText(exampleItem.getExampleName());
            holder.item.setEnabled(exampleItem.isDone());
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (exampleItem instanceof ExampleInfo) {
                    String activityFrom = ((ExampleInfo)exampleItem).getActivityFrom();
                    if (!TextUtils.isEmpty(activityFrom)) {
                        Intent intent = new Intent();
                        ComponentName cn = new ComponentName(getContext(), activityFrom);
                        intent.setComponent(cn);
                        intent.putExtra("title", exampleItem.getExampleName());
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Activity package not found.", Toast.LENGTH_SHORT).show();
                    }
                } else if (exampleItem instanceof ExampleCategory) {
                    mParentCategory = (ExampleCategory)exampleItem;
                    ExampleItemAdapter.this.clear();
                    ExampleItemAdapter.this.addAll(mParentCategory.getItems());
                    notifyDataSetChanged();
                }
                }
            });

            return convertView;
        }

        public class ViewHolder {
            public Button item;
        }
    }
}