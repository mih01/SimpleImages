package heli3a.org.simpleimages;

import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by Hoenisch on 08.07.2015.
 */
public class AnimationTools {

    public static void animate(View view, long time) {
        ViewCompat.setScaleX(view, 0f);
        ViewCompat.setScaleY(view, 0f);
        ViewCompat.setAlpha(view, 0f);
        ViewCompat.animate(view).setDuration(time).scaleX(1f).scaleY(1f).alpha(1f).start();
    }
}
