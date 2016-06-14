package me.kaede.tagview;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.demo_cloud_tagview.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Android TagView Widget
 */
public class TagView extends RelativeLayout {
    public static final String TAG = "TagView";

    private List<Tag> mTags = new ArrayList<>();
    private LayoutInflater mInflater;
    private OnTagClickListener mClickListener;
    private OnTagDeleteListener mDeleteListener;
    private int mWidth;
    private boolean mIsInit = false;
    private boolean mNeedDraw = false;
    private int lineMargin;
    private int tagMargin;
    private int textPaddingLeft;
    private int textPaddingRight;
    private int textPaddingTop;
    private int texPaddingBottom;

    public TagView(Context ctx) {
        super(ctx, null);
        init(ctx, null, 0);
    }

    public TagView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx, attrs, 0);
    }

    public TagView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        init(ctx, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        Constants.DEBUG = (context.getApplicationContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        LogUtil.d(TAG,"[init]");
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // get AttributeSet
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.TagView, defStyle, defStyle);
        this.lineMargin = (int) typeArray.getDimension(R.styleable.TagView_lineMargin, ResolutionUtil.dpToPx(this.getContext(), Constants.DEFAULT_LINE_MARGIN));
        this.tagMargin = (int) typeArray.getDimension(R.styleable.TagView_tagMargin, ResolutionUtil.dpToPx(this.getContext(), Constants.DEFAULT_TAG_MARGIN));
        this.textPaddingLeft = (int) typeArray.getDimension(R.styleable.TagView_textPaddingLeft, ResolutionUtil.dpToPx(this.getContext(), Constants.DEFAULT_TAG_TEXT_PADDING_LEFT));
        this.textPaddingRight = (int) typeArray.getDimension(R.styleable.TagView_textPaddingRight, ResolutionUtil.dpToPx(this.getContext(), Constants.DEFAULT_TAG_TEXT_PADDING_RIGHT));
        this.textPaddingTop = (int) typeArray.getDimension(R.styleable.TagView_textPaddingTop, ResolutionUtil.dpToPx(this.getContext(), Constants.DEFAULT_TAG_TEXT_PADDING_TOP));
        this.texPaddingBottom = (int) typeArray.getDimension(R.styleable.TagView_textPaddingBottom, ResolutionUtil.dpToPx(this.getContext(), Constants.DEFAULT_TAG_TEXT_PADDING_BOTTOM));
        typeArray.recycle();
        mWidth = ResolutionUtil.getScreenWidth(context);
        mIsInit = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LogUtil.d(TAG,"[onSizeChanged]w = " + w);
        mWidth = w;
        if (!mIsInit) {
            mIsInit = true;
            drawTags();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        LogUtil.d(TAG,"[onMeasure]getMeasuredWidth = " + width);
        if (width <= 0) return;
        if (width != mWidth) {
            mWidth = getMeasuredWidth();
            mNeedDraw = true;
            drawTags();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // View#onDraw is disabled in view group;
        // enable View#onDraw for view group : View#setWillNotDraw(false);
        LogUtil.d(TAG,"[onDraw]");
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        LogUtil.d(TAG,"[onVisibilityChanged]");
        if (changedView == this){
            if (visibility == View.VISIBLE){
                drawTags();
            }
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    private void drawTags() {
        LogUtil.d(TAG,"[drawTags]mIsInit = " + mIsInit + ", mNeedDraw = " + mNeedDraw);
        if (!mIsInit) return;
        if (getVisibility() != View.VISIBLE) return;
        LogUtil.i(TAG,"[drawTags]add tags");
        // clear all tag
        removeAllViews();
        // layout padding left & layout padding right
        float total = getPaddingLeft() + getPaddingRight();
        int listIndex = 1;// List Index
        int index_bottom = 1;// The Tag to add below
        int index_header = 1;// The header tag of this line
        Tag tag_pre = null;
        for (Tag item : mTags) {
            final int position = listIndex - 1;
            final Tag tag = item;
            // inflate tag layout
            View tagLayout = (View) mInflater.inflate(R.layout.tagview_item, null);
            tagLayout.setId(listIndex);
            tagLayout.setBackgroundDrawable(getSelector(tag));
            // tag text
            TextView tagView = (TextView) tagLayout.findViewById(R.id.tv_tag_item_contain);
            tagView.setText(tag.text);
            //tagView.setPadding(textPaddingLeft, textPaddingTop, textPaddingRight, texPaddingBottom);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tagView.getLayoutParams();
            params.setMargins(textPaddingLeft, textPaddingTop, textPaddingRight, texPaddingBottom);
            tagView.setLayoutParams(params);
            tagView.setTextColor(tag.tagTextColor);
            tagView.setTextSize(TypedValue.COMPLEX_UNIT_SP, tag.tagTextSize);
            tagLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) {
                        mClickListener.onTagClick(position, tag);
                    }
                }
            });
            // calculate　of tag layout width
            float tagWidth = tagView.getPaint().measureText(tag.text) + textPaddingLeft + textPaddingRight;
            // tagView padding (left & right)
            // deletable text
            TextView deletableView = (TextView) tagLayout.findViewById(R.id.tv_tag_item_delete);
            if (tag.isDeletable) {
                deletableView.setVisibility(View.VISIBLE);
                deletableView.setText(tag.deleteIcon);
                int offset = ResolutionUtil.dpToPx(getContext(), 2f);
                deletableView.setPadding(offset, textPaddingTop, textPaddingRight + offset, texPaddingBottom);
                /*params = (LinearLayout.LayoutParams) deletableView.getLayoutParams();
				params.setMargins(offset, textPaddingTop, textPaddingRight+offset, texPaddingBottom);
				deletableView.setLayoutParams(params);*/
                deletableView.setTextColor(tag.deleteIndicatorColor);
                deletableView.setTextSize(TypedValue.COMPLEX_UNIT_SP, tag.deleteIndicatorSize);
                deletableView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TagView.this.remove(position);
                        if (mDeleteListener != null) {
                            mDeleteListener.onTagDeleted(position, tag);
                        }
                    }
                });
                tagWidth += deletableView.getPaint().measureText(tag.deleteIcon) + deletableView.getPaddingLeft() + deletableView.getPaddingRight();
                // deletableView Padding (left & right)
            } else {
                deletableView.setVisibility(View.GONE);
            }
            LayoutParams tagParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //tagParams.setMargins(0, 0, 0, 0);
            //add margin of each line
            tagParams.bottomMargin = lineMargin;
            if (mWidth <= total + tagMargin + tagWidth + ResolutionUtil.dpToPx(this.getContext(), Constants.LAYOUT_WIDTH_OFFSET)) {
                //need to add in new line
                tagParams.addRule(RelativeLayout.BELOW, index_bottom);
                // initialize total param (layout padding left & layout padding right)
                total = getPaddingLeft() + getPaddingRight();
                index_bottom = listIndex;
                index_header = listIndex;
            } else {
                //no need to new line
                tagParams.addRule(RelativeLayout.ALIGN_TOP, index_header);
                //not header of the line
                if (listIndex != index_header) {
                    tagParams.addRule(RelativeLayout.RIGHT_OF, listIndex - 1);
                    tagParams.leftMargin = tagMargin;
                    total += tagMargin;
                    if (tag_pre.tagTextSize < tag.tagTextSize) {
                        index_bottom = listIndex;
                    }
                }
            }
            total += tagWidth;
            addView(tagLayout, tagParams);
            tag_pre = tag;
            listIndex++;
        }
        mNeedDraw = false;
    }

    private Drawable getSelector(Tag tag) {
        if (tag.background != null) return tag.background;
        StateListDrawable states = new StateListDrawable();
        GradientDrawable gd_normal = new GradientDrawable();
        gd_normal.setColor(tag.layoutColor);
        gd_normal.setCornerRadius(tag.radius);
        if (tag.layoutBorderSize > 0) {
            gd_normal.setStroke(ResolutionUtil.dpToPx(getContext(), tag.layoutBorderSize), tag.layoutBorderColor);
        }
        GradientDrawable gd_press = new GradientDrawable();
        gd_press.setColor(tag.layoutColorPress);
        gd_press.setCornerRadius(tag.radius);
        states.addState(new int[]{android.R.attr.state_pressed}, gd_press);
        //must add state_pressed first，or state_pressed will not take effect
        states.addState(new int[]{}, gd_normal);
        return states;
    }

    public void addTag(Tag tag) {
        mTags.add(tag);
        mNeedDraw = true;
        drawTags();
    }

    public void addTags(String[] tags) {
        if (tags == null || tags.length <= 0) return;
        for (String item : tags) {
            Tag tag = new Tag(item);
            mTags.add(tag);
        }
        mNeedDraw = true;
        drawTags();
    }

    public List<Tag> getTags() {
        return mTags;
    }

    public void remove(int position) {
        mTags.remove(position);
        drawTags();
    }

    public void removeAllTags() {
        mTags.clear();
        drawTags();
    }

    public int getLineMargin() {
        return lineMargin;
    }

    public void setLineMargin(float lineMargin) {
        this.lineMargin = ResolutionUtil.dpToPx(getContext(), lineMargin);
    }

    public int getTagMargin() {
        return tagMargin;
    }

    public void setTagMargin(float tagMargin) {
        this.tagMargin = ResolutionUtil.dpToPx(getContext(), tagMargin);
    }

    public int getTextPaddingLeft() {
        return textPaddingLeft;
    }

    public void setTextPaddingLeft(float textPaddingLeft) {
        this.textPaddingLeft = ResolutionUtil.dpToPx(getContext(), textPaddingLeft);
    }

    public int getTextPaddingRight() {
        return textPaddingRight;
    }

    public void setTextPaddingRight(float textPaddingRight) {
        this.textPaddingRight = ResolutionUtil.dpToPx(getContext(), textPaddingRight);
    }

    public int getTextPaddingTop() {
        return textPaddingTop;
    }

    public void setTextPaddingTop(float textPaddingTop) {
        this.textPaddingTop = ResolutionUtil.dpToPx(getContext(), textPaddingTop);
    }

    public int getTexPaddingBottom() {
        return texPaddingBottom;
    }

    public void setTexPaddingBottom(float texPaddingBottom) {
        this.texPaddingBottom = ResolutionUtil.dpToPx(getContext(), texPaddingBottom);
    }

    public void setOnTagClickListener(OnTagClickListener clickListener) {
        mClickListener = clickListener;
    }

    public void setOnTagDeleteListener(OnTagDeleteListener deleteListener) {
        mDeleteListener = deleteListener;
    }

}
