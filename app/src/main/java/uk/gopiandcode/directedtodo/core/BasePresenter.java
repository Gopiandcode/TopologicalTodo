package uk.gopiandcode.directedtodo.core;


// Model-view-presenter pattern - Presenter class
// Reference: https://medium.com/wolox-driving-innovation/refactoring-our-code-the-mvp-design-pattern-for-android-463df8678c34

public class BasePresenter<T> {

    private T mViewInstance;

    public BasePresenter(T viewInstance) {
        mViewInstance = viewInstance;
    }

    protected T getView() {
        return mViewInstance;
    }

    public void detatchView() {
        mViewInstance = null;
    }
}
