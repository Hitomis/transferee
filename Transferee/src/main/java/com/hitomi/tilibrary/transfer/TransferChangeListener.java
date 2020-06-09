package com.hitomi.tilibrary.transfer;

import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.hitomi.tilibrary.view.image.TransferImage;
import com.hitomi.tilibrary.view.video.ExoVideoView;

import java.util.List;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;

/**
 * Transferee 布局中页面切换监听器，用于处理以下功能： <br/>
 * <ul>
 *     <li>根据相邻优先加载的规则去加载图片</li>
 *     <li>控制所有页面中视频开始播放和重置</li>
 *     <li>如果开启了 enableHideThumb, 需要实时隐藏 originImage</li>
 *     <li>如果开启了 enableScrollingWithPageChange, 需要实时检查当满足条件时滚动用户的列表并更新 OriginImageList</li>
 * </ul>
 * Created by Vans Z on 2020/5/23.
 */
public class TransferChangeListener extends ViewPager.SimpleOnPageChangeListener {
    private static final String TAG = "TransferChangeListener";
    private TransferLayout transfer;
    private TransferConfig transConfig;
    private int selectedPos;

    TransferChangeListener(TransferLayout transfer, TransferConfig transConfig) {
        this.transfer = transfer;
        this.transConfig = transConfig;
    }

    void updateConfig(TransferConfig config) {
        transConfig = config;
    }

    @Override
    public void onPageSelected(final int position) {
        selectedPos = position;
        transConfig.setNowThumbnailIndex(position);

        if (transConfig.isJustLoadHitPage()) {
            transfer.loadSourceViewOffset(position, 0);
        } else {
            for (int i = 1; i <= transConfig.getOffscreenPageLimit(); i++) {
                transfer.loadSourceViewOffset(position, i);
            }
        }
        bindOperationListener(position);
        controlThumbHide(position);
        if (controlScrollingWithPageChange(position)) {
            // controlScrollingWithPageChange 会异步更新 originImageList，
            // 所以这里也需要使用线程队列去保证在之后再执行一次 controlThumbHide
            transfer.post(new Runnable() {
                @Override
                public void run() {
                    controlThumbHide(position);
                }
            });
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
        if (SCROLL_STATE_IDLE == state) {
            controlViewState(selectedPos);
        }
    }

    /**
     * 计算除去 headerSize 和 footerSize 后的第一个可见项 position 和最后一个可见项 position
     *
     * @param originFirstPos 可能有 headerSize 在内的第一个可见项 position
     * @param originLastPos  可能有 headerSize 和 footerSize 在内的最后一个可见项 position
     * @param itemSize       包含 headerSize 和 footerSize 以及 itemCount 的所有 item 数量
     */
    private int[] calcFirstAndLastVisiblePos(int originFirstPos, int originLastPos, int itemSize) {
        int headerSize = transConfig.getHeaderSize();
        int footerSize = transConfig.getFooterSize();
        int contentSize = itemSize - headerSize - footerSize;
        int firstVisiblePos = originFirstPos;
        firstVisiblePos = firstVisiblePos < headerSize ? 0 : firstVisiblePos - headerSize;
        int lastVisiblePos = originLastPos;
        lastVisiblePos = lastVisiblePos > contentSize ? contentSize - 1 : lastVisiblePos - headerSize;
        return new int[]{firstVisiblePos, lastVisiblePos};
    }

    /**
     * 页面切换的时候，如果开启了 enableScrollingWithPageChange,
     * 需要实时检查当满足条件时滚动列表， 并更新 OriginImageList
     *
     * @param position position 值不受 headerSize 和 footerSize 干扰
     */
    private boolean controlScrollingWithPageChange(int position) {
        if (!transConfig.isEnableScrollingWithPageChange()) return false;
        final RecyclerView recyclerView = transConfig.getRecyclerView();
        AbsListView absListView = transConfig.getListView();
        if (recyclerView == null && absListView == null) return false;

        View scrollView = recyclerView == null ? absListView : recyclerView;
        int firstVisiblePos = -1, lastVisiblePos = -1;
        if (recyclerView != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) { // GridLayoutManager 继承 LinearLayoutManager
                LinearLayoutManager linearLayManager = ((LinearLayoutManager) layoutManager);
                int[] posArray = calcFirstAndLastVisiblePos(
                        linearLayManager.findFirstVisibleItemPosition(),
                        linearLayManager.findLastVisibleItemPosition(),
                        linearLayManager.getItemCount()
                );
                firstVisiblePos = posArray[0];
                lastVisiblePos = posArray[1];
            }
        } else {
            int[] posArray = calcFirstAndLastVisiblePos(
                    absListView.getFirstVisiblePosition(),
                    absListView.getLastVisiblePosition(),
                    absListView.getCount()
            );
            firstVisiblePos = posArray[0];
            lastVisiblePos = posArray[1];
        }
        Log.e(TAG, String.format("position = %s, firstVisiblePos = %s, lastVisiblePos = %s",
                position, firstVisiblePos, lastVisiblePos));

        // item 在列表可见范围之内， 不需要处理
        if (position >= firstVisiblePos && position <= lastVisiblePos) return false;
        final int realPos = position + transConfig.getHeaderSize();
        if (position < firstVisiblePos) { // 跳转位置在第一个可见项之前
            if (recyclerView != null) {
                recyclerView.scrollToPosition(realPos);
            } else {
                absListView.setSelection(realPos);
            }
        } else { // 跳转位置在最后可见项之后
            if (recyclerView != null) {
                recyclerView.scrollToPosition(realPos);
            } else {
                absListView.setSelection(realPos);
            }
        }
        // 执行到这里说明一定发生过滚动
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                OriginalViewHelper.getInstance().fillOriginImages(transConfig);
            }
        });
        return true;
    }

    /**
     * 页面切换的时候，如果开启了 enableHideThumb, 那么需要实时隐藏 originImage
     */
    private void controlThumbHide(int position) {
        if (!transConfig.isEnableHideThumb()) return;
        List<ImageView> originImageList = transConfig.getOriginImageList();
        for (int i = 0; i < originImageList.size(); i++) {
            ImageView currOriginImage = originImageList.get(i);
            if (currOriginImage != null) {
                currOriginImage.setVisibility(i == position ? View.INVISIBLE : View.VISIBLE);
            }
        }
    }

    /**
     * 页面切换的时候，控制视频播放的重置和开始，以及图片的重置
     */
    private void controlViewState(int position) {
        SparseArray<FrameLayout> cacheItems = transfer.transAdapter.getCacheItems();
        for (int i = 0; i < cacheItems.size(); i++) {
            int key = cacheItems.keyAt(i);
            View view = cacheItems.get(key).getChildAt(0);
            if (view instanceof ExoVideoView) {
                ExoVideoView videoView = ((ExoVideoView) view);
                if (key == position) {
                    videoView.play();
                } else {
                    videoView.reset();
                }
            } else if (view instanceof TransferImage) {
                TransferImage imageView = ((TransferImage) view);
                if (!imageView.isPhotoNotChanged()) imageView.reset();
            }
        }
    }

    /**
     * 绑定目前支持的操作事件:
     * <p>1.点击事件</p>
     * <p>2.长按事件</p>
     */
    void bindOperationListener(final int position) {
        final FrameLayout parent = transfer.transAdapter.getParentItem(position);
        if (parent == null || parent.getChildAt(0) == null) return;

        final View contentView = parent.getChildAt(0);
        // 因为 TransferImage 拦截了 click 事件，所以如果是图片，click 事件
        // 只能绑定到 TransferImage，其他情况都绑定到 parent 上面
        View bindClickView;
        if (contentView instanceof TransferImage) {
            bindClickView = contentView;
        } else {
            bindClickView = parent;
        }
        if (!bindClickView.hasOnClickListeners()) {
            bindClickView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transfer.dismiss(position);
                }
            });
        }
        // 只有图片支持长按事件
        if (contentView instanceof TransferImage && transConfig.getLongClickListener() != null) {
            contentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TransferImage targetImage = ((TransferImage) contentView);
                    String sourceUrl = transConfig.getSourceUrlList().get(position);
                    transConfig.getLongClickListener().onLongClick(targetImage, sourceUrl, position);
                    return false;
                }
            });
        }
    }
}
