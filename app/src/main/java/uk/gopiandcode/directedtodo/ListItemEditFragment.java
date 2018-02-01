package uk.gopiandcode.directedtodo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ListItemEditFragment extends Fragment {

    private EditText mDependantText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_list_item, container, false);
        mDependantText = (EditText) view.findViewById(R.id.dependant_text);
        mDependantText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mDependantText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(String.valueOf(Log.DEBUG), "onEditor action");
                Toast.makeText(getActivity(), "OnEditorAction", Toast.LENGTH_LONG).show();
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    Toast.makeText(getActivity(), "Example text", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
