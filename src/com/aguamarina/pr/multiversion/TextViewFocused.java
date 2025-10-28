package cm.aptoide.pt.multiversion;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author rafael
 * @since 2.5.3
 * 
 * @see android.widget.TextView
 * 
 * The purpose of this class was to provide a nice marquee on in a TextView without the need of focus on it.
 */
public class TextViewFocused extends TextView {

	/**
	 * @param context
	 */
	public TextViewFocused(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public TextViewFocused(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public TextViewFocused(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * 
	 * @see android.widget.TextView#onFocusChanged(boolean, int, android.graphics.Rect)
	 */
	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		 
		if(focused)
			super.onFocusChanged(focused, direction, previouslyFocusedRect);

	}
	
	/**
	 * 
	 * @see android.widget.TextView#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		 if(hasWindowFocus)
			 super.onWindowFocusChanged(hasWindowFocus);
	}

	/**
	 * 
	 * @see android.view.View#isFocused()
	 */
	@Override
	public boolean isFocused() { return true; }
	
}
