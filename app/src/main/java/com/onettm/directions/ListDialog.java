package com.onettm.directions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class ListDialog extends DialogFragment {
    View view;
    ListView listView;
    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */


    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(LocationItem locationItem, LocationItem[] destinations);

        public LocationItem[] getDestinations();

    }

    /**
     * A dummy implementation of the {@link com.onettm.directions.ListDialog.Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(LocationItem locationItem, LocationItem[] destinations) {
        }

        @Override
        public LocationItem[] getDestinations() {
            return null;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        AsyncTask<ListView, Void, Object> at = new AsyncTask<ListView, Void, Object>() {

            ProgressDialog progress;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                System.err.println("TIME PREEXECUTE " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
                listView.setVisibility(View.GONE);

                progress = ProgressDialog.show(ListDialog.this.getActivity(), "Loading", "Wait while loading...", true, false);
            }

            @Override
            protected Object doInBackground(ListView... listViews) {
                //Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                System.err.println("TIME doInBackground 1 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
                LocationItem[] result = mCallbacks.getDestinations();
                System.err.println("TIME doInBackground 2 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));
                return result;
            }


            @Override
            protected void onPostExecute(final Object destinations) {
                super.onPostExecute(destinations);
                listView.setVisibility(View.VISIBLE);

                System.err.println("TIME onPostExecute 1 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

                listView.setAdapter(new ArrayAdapter<LocationItem>(
                        getActivity(),
                        android.R.layout.simple_list_item_activated_1,
                        android.R.id.text1, (LocationItem[])destinations
                ));
                System.err.println("TIME onPostExecute 2 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mCallbacks.onItemSelected((LocationItem) parent.getAdapter().getItem(position), (LocationItem[])destinations);
                        dismiss();
                    }
                });
                System.err.println("TIME onPostExecute 3 " + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()));

                progress.dismiss();
            }
        };
        at.execute(listView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_list_dialog, container, false);
        listView = (ListView) view.findViewById(R.id.item_list);

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }


}
