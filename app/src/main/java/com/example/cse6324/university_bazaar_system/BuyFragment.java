package com.example.cse6324.university_bazaar_system;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.google.gson.Gson;

import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BuyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BuyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BuyFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public BuyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BuyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BuyFragment newInstance(String param1, String param2) {
        BuyFragment fragment = new BuyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View myFragmentView = inflater.inflate(R.layout.fragment_buy, container, false);

        mRecyclerView = (RecyclerView) myFragmentView.findViewById(R.id.buy_recycler_view);
        //mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerViewAdapter(((EntryScreenActivity)getActivity()).items, ((EntryScreenActivity)getActivity()).images);
        mRecyclerView.setAdapter(mAdapter);

        return myFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((EntryScreenActivity)getActivity()).setActionBarTitle(R.string.browse);
        ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(Map<String, Object> item, View v) {
                ((InputMethodManager)((EntryScreenActivity)getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(((EntryScreenActivity)getActivity()).getCurrentFocus().getWindowToken(), 0);
                FragmentManager fragMan = getFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putString("item", new Gson().toJson(item));
                Fragment myFrag = MerchandiseDetailFragment.newInstance(bundle);
                FragmentTransaction transaction = fragMan.beginTransaction();
                transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left);
                transaction.replace(R.id.content_entry, myFrag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.buy_search, menu);
        super.onCreateOptionsMenu(menu, inflater);

        final MenuItem search = menu.findItem(R.id.buy_search);
        final SearchView searchView = (SearchView) search.getActionView();
        search(searchView);
        searchView.setIconified(true);
        searchView.setIconified(false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }
/*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
