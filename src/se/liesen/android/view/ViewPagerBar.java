package se.liesen.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class ViewPagerBar extends TextView implements ViewPager.OnPageChangeListener {
  /**
   * Adapter for header text for each page.
   */
  public interface TextAdapter {
    /**
     * 
     * @return
     */
    public int getCount();

    /**
     * 
     * @param position
     * @return
     */
    public String getText(int position);
  }

  /** Current position. */
  private int mPosition;
  
  /** Offset in pixels. */
  private int mOffsetPixels;

  /** Adapter for texts. */
  private TextAdapter mAdapter;

  private TextPaint mHighlightPaint;

  /**
   * Default constructor
   */
  public ViewPagerBar(Context context) {
    super(context);
    initViewPagerBar();
  }

  /**
   * The contructor used with an inflater
   * 
   * @param context
   * @param attrs
   */
  public ViewPagerBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    initViewPagerBar();
  }

  /**
   * @param context
   * @param attrs
   * @param defStyle
   */
  public ViewPagerBar(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initViewPagerBar();
  }

  private void initViewPagerBar() {
    final Paint paint = getPaint();
    final int defaultTextColor = paint.getColor();
    final int selectedTextColor =
        getTextColors().getColorForState(TextView.SELECTED_STATE_SET, defaultTextColor);
    mHighlightPaint = new TextPaint(paint);
    mHighlightPaint.setFakeBoldText(true); // TODO(liesen): Use attribute
    mHighlightPaint.setColor(selectedTextColor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.view.View#onDraw(android.graphics.Canvas)
   */
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    final int count = mAdapter == null ? 0 : mAdapter.getCount();
    
    if (count == 0) {
      return;
    }

    final int width = getWidth();
    final int centerX = width / 2;
    final int paddingLeft = getPaddingLeft();
    final int paddingRight = getPaddingRight();
    Paint paint = getPaint();
    final int baseline = getBaseline();
    final Rect emptyBounds = new Rect(0, 0, paddingLeft + paddingRight, baseline);

    // Measure center text
    final String centerText = mAdapter.getText(mPosition);
    String text = centerText;
    final Rect bounds = new Rect(emptyBounds);
    bounds.right += (int) paint.measureText(text);
    bounds.offset(centerX - bounds.width() / 2 - mOffsetPixels, 0);

    // Clamp to screen
    if (bounds.right > width) {
      bounds.offsetTo(width - bounds.width(), 0);
    }

    if (bounds.left < 0) {
      bounds.offsetTo(0, 0);
    }
    
    final Rect centerBounds = new Rect(bounds); // Save

    // Left text
    if (mPosition > 0) {
      text = mAdapter.getText(mPosition - 1);
      bounds.set(emptyBounds);
      bounds.right += (int) paint.measureText(text);

      // Check overlap with center text
      if (bounds.right > centerBounds.left) {
        bounds.offset(centerBounds.left - bounds.right, 0);
      }

      if (bounds.right > paddingLeft) {
        canvas.drawText(text, bounds.left + paddingLeft, baseline, paint);
      }
    }

    // Right text
    if (mPosition < count - 1) {
      text = mAdapter.getText(mPosition + 1);
      bounds.set(emptyBounds);
      bounds.right += (int) paint.measureText(text);
   
      // Clamp right edge to screen
      bounds.offsetTo(width - bounds.width(), 0);

      // Check overlap with center text
      if (bounds.left < centerBounds.right) {
        bounds.offset(centerBounds.right - bounds.left, 0);
      }

      if (bounds.left < width - paddingRight) {
        canvas.drawText(text, bounds.left + paddingLeft, baseline, paint);
      }
    }
    
    // Restore center text bounds
    bounds.set(centerBounds);

    // Set color on center text
    if (bounds.left < centerX && bounds.right > centerX) {
      paint = mHighlightPaint;
    }

    canvas.drawText(centerText, bounds.left + paddingLeft, baseline, paint);
  }

  public void setAdapter(TextAdapter adapter) {
    mAdapter = adapter;
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    mOffsetPixels = (position - mPosition) * getWidth() + positionOffsetPixels;
    invalidate();
  }

  @Override
  public void onPageSelected(int position) {
    mPosition = position;
  }

  @Override
  public void onPageScrollStateChanged(int state) {
  }
}
