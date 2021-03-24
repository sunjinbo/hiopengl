package com.hiopengl.examples;

import java.util.ArrayList;
import java.util.List;

public class ExampleCategory extends ExampleItem {
    private List<ExampleItem> mItems = new ArrayList();

    public ExampleCategory(String exampleName) {
        super(exampleName, true);
    }

    public ExampleCategory(String exampleName, boolean isDone) {
        super(exampleName, isDone);
    }

    public void addItem(ExampleItem item) {
        mItems.add(item);
    }

    public void addItems(List<ExampleItem> items) {
        mItems.addAll(items);
    }

    public List<ExampleItem> getItems() {
        return mItems;
    }
}
