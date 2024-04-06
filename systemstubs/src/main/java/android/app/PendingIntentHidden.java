package android.app;

import android.content.Intent;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(PendingIntent.class)
public class PendingIntentHidden {

    /**
     * Return the Intent of this PendingIntent.
     */
    public Intent getIntent() {
        throw new RuntimeException("Stub!");
    }

}
