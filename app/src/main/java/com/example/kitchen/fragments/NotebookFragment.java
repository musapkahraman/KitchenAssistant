package com.example.kitchen.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.kitchen.R;
import com.example.kitchen.activities.MainActivity;
import com.example.kitchen.adapters.NotebookAdapter;
import com.example.kitchen.adapters.RecipeClickListener;
import com.example.kitchen.data.local.entities.Recipe;
import com.example.kitchen.utility.AppConstants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotebookFragment extends Fragment {
    private static final String LOG_TAG = NotebookFragment.class.getSimpleName();
    private static final String LAYOUT_STATE = "state";
    private static final String SEARCH_QUERY = "search-query";
    @BindView(R.id.rv_recipe_steps)
    RecyclerView mRecyclerView;
    private Context mContext;
    private FragmentScrollListener fragmentScrollListener;
    private RecipeClickListener mClickListener;
    private StaggeredGridLayoutManager mLayoutManager;
    private NotebookAdapter mAdapter;
    private String mQuery;

    public NotebookFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof RecipeClickListener) {
            mClickListener = (RecipeClickListener) context;
        } else {
            throw new ClassCastException(context.toString() + "must implement RecipeClickListener");
        }
        if (context instanceof FragmentScrollListener) {
            fragmentScrollListener = (FragmentScrollListener) context;
        } else {
            throw new ClassCastException(context.toString() + "must implement FragmentScrollListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipes, container, false);
        ButterKnife.bind(this, rootView);
        if (getResources().getBoolean(R.bool.landscape)) {
            mLayoutManager = new StaggeredGridLayoutManager(1, RecyclerView.HORIZONTAL);
        } else {
            mLayoutManager = new StaggeredGridLayoutManager(1, RecyclerView.VERTICAL);
        }
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new NotebookAdapter(mContext, mClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0 || dy > 0) {
                    fragmentScrollListener.onScrollDown();
                } else if (dx < 0 || dy < 0) {
                    fragmentScrollListener.onScrollUp();
                }
            }
        });
        Bundle arguments = getArguments();
        if (arguments != null) {
            List<Recipe> recipes = arguments.getParcelableArrayList(AppConstants.KEY_RECIPES);
            mAdapter.setRecipes(recipes);
            savedInstanceState = arguments.getBundle(AppConstants.KEY_SAVED_STATE);
            if (savedInstanceState != null) {
                mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(LAYOUT_STATE));
                mQuery = savedInstanceState.getString(SEARCH_QUERY);
                mAdapter.filter(mQuery);
            }
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        sendToActivity(outState);
    }

    @Override
    public void onDetach() {
        mClickListener = null;
        fragmentScrollListener = null;
        super.onDetach();
    }

    private void sendToActivity(Bundle outState) {
        outState.putParcelable(LAYOUT_STATE, mLayoutManager.onSaveInstanceState());
        outState.putString(SEARCH_QUERY, mQuery);
        MainActivity activity = null;
        try {
            activity = (MainActivity) getActivity();
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        if (activity != null) {
            activity.fromNotebookFragment(outState);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Clear all current menu items
        menu.clear();
        // Add new menu items
        inflater.inflate(R.menu.menu_recipes, menu);
        // Associate searchable configuration with the SearchView
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.filter(query);
                mQuery = query;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                mQuery = newText;
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.app_bar_filter:
                return true;
            case R.id.app_bar_sort:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}