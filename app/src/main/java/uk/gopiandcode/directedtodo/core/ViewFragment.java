package uk.gopiandcode.directedtodo.core;

// Model-view-presenter pattern - View class
// Reference: https://medium.com/wolox-driving-innovation/refactoring-our-code-the-mvp-design-pattern-for-android-463df8678c34

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ViewFragment<T extends BasePresenter> extends Fragment {
   protected T mPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
       View v = inflater.inflate(layout(), container, false);
       mPresenter = createPresenter();
       setUi(v);
       init();
       populate();
       setListeners();
       return v;
    }

    /**
     * Sets the listeners for the views of the fragment
     */
    protected abstract void setListeners();

    /**
     * Populates the view elements for the fragment
     *
     * I.e would tell the presenter to load stuff
     */
    protected abstract void populate();

    /**
     * Initializes the variables the fragment needs
     */
    protected abstract void init();

    /**
     * Loads the view elements for the view
     * @param v the view generated from the layout
     */
    protected abstract void setUi(View v);

    /**
     * Create the presenter for the fragment
     * @return the presenter
     */
    protected abstract T createPresenter();

    /**
     * Return the layout id for the inflater
     * @return the layout
     */
    protected abstract int layout();
}
