package android.view;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(View.class)
public class ViewHidden {

    public ViewRootImpl getViewRootImpl() {
        throw new RuntimeException("Stub!");
    }

}
