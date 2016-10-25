package de.symeda.sormas.app.task;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.List;

import de.symeda.sormas.api.caze.CaseStatus;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.task.Task;
import de.symeda.sormas.app.caze.CaseEditActivity;
import de.symeda.sormas.app.caze.CasesListArrayAdapter;

/**
 * Created by Stefan Szczesny on 24.10.2016.
 */
public class TasksListFragment extends ListFragment {

    public static final String ARG_FILTER_STATUS = "filterStatus";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cases_list_layout, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Task> tasks;
        Bundle arguments = getArguments();
        /*
        if (arguments.containsKey(ARG_FILTER_STATUS)) {
            TaskStatus filterStatus = (CaseStatus)arguments.getSerializable(ARG_FILTER_STATUS);
            tasks = DatabaseHelper.getCaseDao().queryForEq(Case.CASE_STATUS, filterStatus);
        } else {
            tasks = DatabaseHelper.getCaseDao().queryForAll();
        }
        */

        tasks = DatabaseHelper.getTaskDao().queryForAll();

        ArrayAdapter<Task> listAdapter = (ArrayAdapter<Task>)getListAdapter();
        listAdapter.clear();
        listAdapter.addAll(tasks);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TasksListArrayAdapter adapter = new TasksListArrayAdapter(
                this.getActivity(),           // Context for the activity.
                R.layout.tasks_list_item);    // Layout to use (create)

        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> parent,
                    View viewClicked,
                    int position, long id) {
                Case caze = (Case)getListAdapter().getItem(position);
                showCaseEditView(caze);
            }
        });
    }

    public void showCaseEditView(Case caze) {
        Intent intent = new Intent(getActivity(), CaseEditActivity.class);
        intent.putExtra(CaseEditActivity.KEY_CASE_UUID, caze.getUuid());
        startActivity(intent);
    }
}
