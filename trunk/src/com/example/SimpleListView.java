package com.example;

import java.util.ArrayList;
import java.util.BitSet;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

 public class SimpleListView extends ListView {  
   
     final ArrayList<View> mViews = new ArrayList<View>();  
     final ArrayList<Object> mData = new ArrayList<Object>();  
     final BitSet mEnabled = new BitSet();  
     private final SimpleAdapter mAdapter = new SimpleAdapter();  
     private int mScrollRange;  
     private boolean mScrollToTopPending;  
   
     class SimpleAdapter extends BaseAdapter {  
         public int getCount() {  
             return mViews.size();  
         }  
   
         public Object getItem(int position) {  
             return mData.get(position);  
         }  
   
         public long getItemId(int position) {  
             return position;  
         }  
   
         public View getView(int position, View convertView, ViewGroup parent) {  
             return mViews.get(position);  
         }  
   
         @Override  
         public boolean areAllItemsEnabled() {  
             return false;  
         }  
   
         @Override  
         public boolean isEnabled(int position) {  
             return mEnabled.get(position);  
         }  
   
         @Override  
         public int getItemViewType(int position) {  
             // Don't let ListView try to reuse the views.  
             return AdapterView.ITEM_VIEW_TYPE_IGNORE;  
         }  
     }  
   
     public SimpleListView(Context context) {  
         super(context);  
         setAdapter(mAdapter);  
     }  
   
     public SimpleListView(Context context, AttributeSet attrs) {  
         super(context, attrs);  
         setAdapter(mAdapter);  
     }  
   
     public SimpleListView(Context context, AttributeSet attrs, int defStyle) {  
         super(context, attrs, defStyle);  
         setAdapter(mAdapter);  
     }  
   
     public void addView(View view, Object data, boolean enabled) {  
         mViews.add(view);  
         mData.add(data);  
         if (enabled) mEnabled.set(mViews.size() - 1);  
         mAdapter.notifyDataSetChanged();  
     }  
   
     public void clear() {  
         mViews.clear();  
         mData.clear();  
         mEnabled.clear();  
         mAdapter.notifyDataSetChanged();  
         scrollToTop();  
     }  
   
     private static final Rect RECT = new Rect(0, 0, 1, 1);  
   
     @Override  
     protected void layoutChildren() {  
         super.layoutChildren();  
   
         int scrollRange = 0;  
         for (int i = 0, n = mViews.size(); i < n; i++) {  
             int height = mViews.get(i).getHeight();  
             // Height == 0 means that the view has not been layout-ed.  
             if (height == 0) height = 64;  
             scrollRange += height;  
         }  
         mScrollRange = scrollRange;  
   
         if (mScrollToTopPending && !mViews.isEmpty()) {  
             requestChildRectangleOnScreen(mViews.get(0), RECT, true);  
             mScrollToTopPending = false;  
         }  
     }  
   
     @Override  
     protected int computeVerticalScrollExtent() {  
         return getHeight();  
     }  
   
     @Override  
     protected int computeVerticalScrollOffset() {  
         int result = 0;  
         if (mViews.size() > 0) {  
             int firstVisible = getFirstVisiblePosition();  
             for (int i = 0; i < firstVisible; i++) {  
                 result += mViews.get(i).getHeight();  
             }  
             result -= mViews.get(firstVisible).getTop();  
         }  
         return result;  
     }  
   
     @Override  
     protected int computeVerticalScrollRange() {  
         return mScrollRange;  
     }  
   
     public void scrollToTop() {  
         setSelection(0);  
         mScrollToTopPending = true;  
     }  
   
 }  