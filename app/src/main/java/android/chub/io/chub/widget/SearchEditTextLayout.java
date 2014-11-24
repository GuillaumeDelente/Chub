package android.chub.io.chub.widget;/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.chub.io.chub.R;
import android.chub.io.chub.animation.AnimUtils;
import android.chub.io.chub.util.DialerUtils;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class SearchEditTextLayout extends FrameLayout {
    private static final float EXPAND_MARGIN_FRACTION_START = 0.8f;
    private static final int ANIMATION_DURATION = 200;

    private OnKeyListener mPreImeKeyListener;
    private int mTopMargin;
    private int mBottomMargin;
    private int mLeftMargin;
    private int mRightMargin;

    private float mCollapsedElevation;

    /* Subclass-visible for testing */
    protected boolean mIsExpanded = false;
    protected boolean mIsFadedOut = false;

    private View mCollapsed;
    private View mExpanded;
    private EditText mSearchView;
    private View mSearchIcon;
    private View mCollapsedSearchBox;
    private View mClearButton;
    private View mOverflowButtonView;
    private View mBackButtonView;
    private View mExpandedSearchBox;
    private View mCollapsedClearButton;
    private TextView mSearchTextView;

    private ValueAnimator mAnimator;

    private OnBackButtonClickedListener mOnBackButtonClickedListener;
    private OnClickListener mOnClearButtonViewClickListener;

    public void setOnClearButtonViewClickListener(OnClickListener onClearButtonViewClickListener) {
        mOnClearButtonViewClickListener = onClearButtonViewClickListener;
    }

    /**
     * Listener for the back button next to the search view being pressed
     */
    public interface OnBackButtonClickedListener {
        public void onBackButtonClicked();
    }

    public SearchEditTextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPreImeKeyListener(OnKeyListener listener) {
        mPreImeKeyListener = listener;
    }

    public void setOnBackButtonClickedListener(OnBackButtonClickedListener listener) {
        mOnBackButtonClickedListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        mTopMargin = params.topMargin;
        mBottomMargin = params.bottomMargin;
        mLeftMargin = params.leftMargin;
        mRightMargin = params.rightMargin;

        mCollapsedElevation = getElevation();

        mCollapsed = findViewById(R.id.search_box_collapsed);
        mExpanded = findViewById(R.id.search_box_expanded);
        mSearchView = (EditText) mExpanded.findViewById(R.id.search_view);

        mSearchIcon = findViewById(R.id.search_magnifying_glass);
        mCollapsedSearchBox = findViewById(R.id.search_box_start_search);
        mClearButton = findViewById(R.id.search_close_button);
        mOverflowButtonView = findViewById(R.id.dialtacts_options_menu_button);
        mBackButtonView = findViewById(R.id.search_back_button);
        mExpandedSearchBox = findViewById(R.id.search_box_expanded);
        mCollapsedClearButton = findViewById(R.id.collapsed_clear_button);
        mCollapsedClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClearButtonViewClickListener != null)
                    mOnClearButtonViewClickListener.onClick(view);
                mSearchView.setText(null);
                setCollapsedSearchBoxText(null);
            }
        });
        mSearchTextView = (TextView) findViewById(R.id.search_box_textview);
        mSearchView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DialerUtils.showInputMethod(v);
                } else {
                    DialerUtils.hideInputMethod(v);
                }
            }
        });

        findViewById(R.id.search_close_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.setText(null);
            }
        });

        findViewById(R.id.search_back_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBackButtonClickedListener != null) {
                    mOnBackButtonClickedListener.onBackButtonClicked();
                }
            }
        });
        super.onFinishInflate();
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (mPreImeKeyListener != null) {
            if (mPreImeKeyListener.onKey(this, event.getKeyCode(), event)) {
                return true;
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    public void fadeOut() {
        fadeOut(null);
    }

    public void fadeOut(AnimUtils.AnimationCallback callback) {
        AnimUtils.fadeOut(this, ANIMATION_DURATION, callback);
        mIsFadedOut = true;
    }

    public void fadeIn() {
        AnimUtils.fadeIn(this, ANIMATION_DURATION);
        mIsFadedOut = false;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            setAlpha(1);
            setVisibility(View.VISIBLE);
            mIsFadedOut = false;
        } else {
            setAlpha(0);
            setVisibility(View.GONE);
            mIsFadedOut = true;
        }
    }

    public void setCollapsedSearchBoxText(String text) {
        if (TextUtils.isEmpty(text)) {
            mCollapsedClearButton.setVisibility(View.GONE);
        } else {
            mCollapsedClearButton.setVisibility(View.VISIBLE);
        }
        mSearchTextView.setText(text);
    }

    public void expand(boolean animate, boolean requestFocus) {
        updateVisibility(true /* isExpand */);

        if (animate) {
            AnimUtils.crossFadeViews(mExpanded, mCollapsed, ANIMATION_DURATION);
            mAnimator = ValueAnimator.ofFloat(EXPAND_MARGIN_FRACTION_START, 0f);
            setMargins(EXPAND_MARGIN_FRACTION_START);
            prepareAnimator(true);
        } else {
            mExpanded.setVisibility(View.VISIBLE);
            mExpanded.setAlpha(1);
            setMargins(0f);
            mCollapsed.setVisibility(View.GONE);
        }

        // Set 9-patch background. This owns the padding, so we need to restore the original values.
        int paddingTop = this.getPaddingTop();
        int paddingStart = this.getPaddingStart();
        int paddingBottom = this.getPaddingBottom();
        int paddingEnd = this.getPaddingEnd();
        setBackgroundResource(R.drawable.search_shadow);
        setElevation(0);
        setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom);

        setElevation(0);
        if (requestFocus) {
            mSearchView.requestFocus();
        }
        mIsExpanded = true;
    }

    public void collapse(boolean animate) {
        updateVisibility(false /* isExpand */);

        if (animate) {
            AnimUtils.crossFadeViews(mCollapsed, mExpanded, ANIMATION_DURATION);
            mAnimator = ValueAnimator.ofFloat(0f, 1f);
            prepareAnimator(false);
        } else {
            mCollapsed.setVisibility(View.VISIBLE);
            mCollapsed.setAlpha(1);
            setMargins(1f);
            mExpanded.setVisibility(View.GONE);
        }

        mIsExpanded = false;
        setElevation(mCollapsedElevation);
        setBackgroundResource(R.drawable.rounded_corner);
    }

    /**
     * Updates the visibility of views depending on whether we will show the expanded or collapsed
     * search view. This helps prevent some jank with the crossfading if we are animating.
     *
     * @param isExpand Whether we are about to show the expanded search box.
     */
    private void updateVisibility(boolean isExpand) {
        int collapsedViewVisibility = isExpand ? View.GONE : View.VISIBLE;
        int expandedViewVisibility = isExpand ? View.VISIBLE : View.GONE;

        mSearchIcon.setVisibility(collapsedViewVisibility);
        mCollapsedSearchBox.setVisibility(collapsedViewVisibility);
        mCollapsedClearButton.setVisibility(TextUtils.isEmpty(mSearchTextView.getText()) ? View.GONE :
                collapsedViewVisibility);
        mOverflowButtonView.setVisibility(collapsedViewVisibility);
        mBackButtonView.setVisibility(expandedViewVisibility);
        // TODO: Prevents keyboard from jumping up in landscape mode after exiting the
        // SearchFragment when the query string is empty. More elegant fix?
        //mExpandedSearchBox.setVisibility(expandedViewVisibility);
        mClearButton.setVisibility(expandedViewVisibility);
    }

    private void prepareAnimator(final boolean expand) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final Float fraction = (Float) animation.getAnimatedValue();
                setMargins(fraction);
            }
        });

        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.start();
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public boolean isFadedOut() {
        return mIsFadedOut;
    }

    /**
     * Assigns margins to the search box as a fraction of its maximum margin size
     *
     * @param fraction How large the margins should be as a fraction of their full size
     */
    private void setMargins(float fraction) {
        MarginLayoutParams params = (MarginLayoutParams) getLayoutParams();
        params.topMargin = (int) (mTopMargin * fraction);
        params.bottomMargin = (int) (mBottomMargin * fraction);
        params.leftMargin = (int) (mLeftMargin * fraction);
        params.rightMargin = (int) (mRightMargin * fraction);
        requestLayout();
    }
}
