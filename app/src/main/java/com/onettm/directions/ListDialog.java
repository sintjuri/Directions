package com.onettm.directions;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Collection;


public class ListDialog extends DialogFragment {
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

    }

    /**
     * A dummy implementation of the {@link com.onettm.directions.ListDialog.Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(LocationItem locationItem, LocationItem[] destinations) {
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Collection<LocationItem> result = DirectionsApplication.getInstance().getLocationsManager().getLocationItems();

        final LocationItem[] res;
        res = result.toArray(new LocationItem[0]);

        listView.setVisibility(View.VISIBLE);

        /*listView.setAdapter(new ArrayAdapter<LocationItem>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1, res
        ));*/

        final Model model = DirectionsApplication.getInstance().getModel();
        final Data modelData = model.getData();

        listView.setAdapter(new CompassArrayAdapter(getActivity(), res, modelData));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onItemSelected((LocationItem) parent.getAdapter().getItem(position), res);
                dismiss();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_dialog, container, false);
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
