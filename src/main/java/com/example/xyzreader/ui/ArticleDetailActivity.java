package com.example.xyzreader.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.android.volley.toolbox.NetworkImageView;
import com.example.xyzreader.R;
import com.example.xyzreader.data.Article;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private ViewPager mPager;
    private NetworkImageView mPhotoView;
    private MyPagerAdapter mPagerAdapter;
    private Toolbar toolbar;
    private AppBarLayout appBar;
    private CollapsingToolbarLayout collapsingToolbar;
    private Article currentArticle;
    private boolean isToolbarCollapsed = false;
    private int mCurrentPosition;
    private int mStartingPosition;
    private boolean mIsReturning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        getSupportLoaderManager().initLoader(0, null, this);

        mPhotoView = (NetworkImageView) findViewById(R.id.photo);
        appBar = (AppBarLayout) findViewById(R.id.appbar);

        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                float density = metrics.density;
                if (appBarLayout.getHeight() - (24 * density) + verticalOffset > ViewCompat.getMinimumHeight(collapsingToolbar)) {
                    collapsingToolbar.setTitle("");
                    if (isToolbarCollapsed) {
                        isToolbarCollapsed = false;

                        int colorFrom = getResources().getColor(R.color.theme_primary);
                        int colorTo = getResources().getColor(android.R.color.transparent);
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(500);
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                collapsingToolbar.setContentScrimColor((int) animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.start();
                    }
                } else {
                    if (currentArticle != null)
                        collapsingToolbar.setTitle(currentArticle.getTitle());
                    collapsingToolbar.setContentScrimColor(getResources().getColor(R.color.theme_primary));
                    isToolbarCollapsed = true;
                }
            }
        });

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentArticle != null) {
                    startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/html")
                            .setSubject(currentArticle.getTitle())
                            .setHtmlText(currentArticle.getBody())
                            .getIntent(), getString(R.string.action_share)));
                }
            }
        });

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    setCurrentArticle(position);
                    refresh();
                }
            }
        });


        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        if (upArrow != null)
            upArrow.setColorFilter(getResources().getColor(R.color.window_background), PorterDuff.Mode.SRC_ATOP);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartingPosition = getIntent().getIntExtra(ArticleListActivity.STARTING_POSITION, 0);
                mCurrentPosition = mStartingPosition;
                mPhotoView.setTransitionName(getResources().getString(R.string.photo_transition) + String.valueOf(mStartingPosition));
            }
        }
        else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (mIsReturning) {
                        if (mPhotoView == null || isToolbarCollapsed) {
                            names.clear();
                            sharedElements.clear();
                        } else {
                            names.clear();
                            names.add(mPhotoView.getTransitionName());
                            sharedElements.clear();
                            sharedElements.put(mPhotoView.getTransitionName(), mPhotoView);
                        }
                    }
                }
                @Override
                public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                    super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setHomeAsUpIndicator(upArrow);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    }
                }
            });
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(ArticleListActivity.STARTING_POSITION, mStartingPosition);
        data.putExtra(ArticleListActivity.CURRENT_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (currentArticle != null) {
            outState.putParcelable("current_article", currentArticle);
        }
        outState.putInt(ArticleListActivity.STARTING_POSITION, mStartingPosition);
        outState.putInt(ArticleListActivity.CURRENT_POSITION, mCurrentPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentArticle = savedInstanceState.getParcelable("current_article");
        mStartingPosition = savedInstanceState.getInt(ArticleListActivity.STARTING_POSITION, 0);
        mCurrentPosition = savedInstanceState.getInt(ArticleListActivity.CURRENT_POSITION, 0);
        mPhotoView.setTransitionName(getResources().getString(R.string.photo_transition) + String.valueOf(mCurrentPosition));
        refresh();
    }

    private Activity getActivity() {
        return this;
    }

    public void setCurrentArticle(int position) {
        mCurrentPosition = position;
        mCursor.moveToPosition(mCurrentPosition);
        long id = mCursor.getLong(ArticleLoader.Query._ID);
        String title = mCursor.getString(ArticleLoader.Query.TITLE);
        long published_date = mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE);
        String author = mCursor.getString(ArticleLoader.Query.AUTHOR);
        String thumb_url = mCursor.getString(ArticleLoader.Query.THUMB_URL);
        String photo_url = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
        float aspect_ratio = mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);
        String body = mCursor.getString(ArticleLoader.Query.BODY);
        this.currentArticle = new Article(id, title, published_date, author, thumb_url, photo_url, aspect_ratio, body);
        mPhotoView.setTransitionName(getResources().getString(R.string.photo_transition) + String.valueOf(mCurrentPosition));
    }

    public void refresh() {
        if (currentArticle != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeout);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
                    mPhotoView.setImageUrl(
                            currentArticle.getPhotoUrl(),
                            ImageLoaderHelper.getInstance(ArticleDetailActivity.this).getImageLoader());
                    mPhotoView.setAnimation(fadeIn);
                }
            });
            mPhotoView.startAnimation(fadeOut);
            if (isToolbarCollapsed) {
                collapsingToolbar.setTitle(currentArticle.getTitle());
            }
            else {
                collapsingToolbar.setTitle("");
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();
        if (mStartingPosition >= 0) {
            mPager.setCurrentItem(mStartingPosition, false);
            setCurrentArticle(mStartingPosition);
            refresh();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
