package cm.aptoide.pt.multiversion;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.Button;

public class ButtonFocused extends Button{
	
	/**
	 * @param context
	 */
	public ButtonFocused(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ButtonFocused(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ButtonFocused(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * 
	 * @see android.widget.Button#onFocusChanged(boolean, int, android.graphics.Rect)
	 */
	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		 
		if(focused){
			super.onFocusChanged(focused, direction, previouslyFocusedRect);
		}

	}
	
	/**
	 * 
	 * @see android.widget.Button#onWindowFocusChanged(boolean)
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
	public boolean isFocused() { 
		/*
		 * Some layout issues relative to the migration of the option down grade from the tabupdates to the tab installed were resolved.
A bug that causes null to be displayed when a external download occurs was fixed.
		 * 
		 * */
		return true; }
	
}
