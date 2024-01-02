package com.runeanim.mytoyproject.util

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider

class StartSnapHelper : PagerSnapHelper() {
    private lateinit var mVerticalHelper: OrientationHelper
    private lateinit var mHorizontalHelper: OrientationHelper

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        return intArrayOf(
            if (layoutManager.canScrollHorizontally()) distanceToStart(targetView, getHorizontalHelper(layoutManager)) else 0,
            if (layoutManager.canScrollVertically()) distanceToStart(targetView, getVerticalHelper(layoutManager)) else 0
        )
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return if (layoutManager is LinearLayoutManager) {
            getStartView(layoutManager, getOrientationHelper(layoutManager))
        } else {
            super.findSnapView(layoutManager)
        }
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    private fun getStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper?): View? {
        if (layoutManager is LinearLayoutManager) {
            var firstChild = layoutManager.findFirstVisibleItemPosition()
            val isLastItem = (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1)

            if (firstChild == RecyclerView.NO_POSITION) {
                return null
            }
            if (helper == null) {
                return null
            }
            if (isLastItem) {
                firstChild = layoutManager.getItemCount() - 1
                return layoutManager.findViewByPosition(firstChild)
            }

            val child = layoutManager.findViewByPosition(firstChild)

            return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2 && helper.getDecoratedEnd(child) > 0) {
                child
            } else {
                layoutManager.findViewByPosition(firstChild + 1)
            }
        }
        return super.findSnapView(layoutManager)
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (::mVerticalHelper.isInitialized.not()) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (::mHorizontalHelper.isInitialized.not()) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper
    }

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        val orientationHelper = getOrientationHelper(layoutManager) ?: return RecyclerView.NO_POSITION

        var closestChildBeforeCenter: View? = null
        var distanceBefore = Int.MIN_VALUE
        var closestChildAfterCenter: View? = null
        var distanceAfter = Int.MAX_VALUE

        val childCount = layoutManager.childCount
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val distance = distanceToStart(child, orientationHelper)
            if (distance in (distanceBefore + 1)..0) {
                distanceBefore = distance
                closestChildBeforeCenter = child
            }
            if (distance in 0 until distanceAfter) {
                distanceAfter = distance
                closestChildAfterCenter = child
            }
        }

        val forwardDirection = isForwardFling(layoutManager, velocityX, velocityY)
        if (forwardDirection && closestChildAfterCenter != null) {
            return layoutManager.getPosition(closestChildAfterCenter)
        } else if (!forwardDirection && closestChildBeforeCenter != null) {
            return layoutManager.getPosition(closestChildBeforeCenter)
        }

        val visibleView = (if (forwardDirection) closestChildBeforeCenter else closestChildAfterCenter) ?: return RecyclerView.NO_POSITION
        val visiblePosition = layoutManager.getPosition(visibleView)
        val snapToPosition = (visiblePosition + if (isReverseLayout(layoutManager) == forwardDirection) -1 else +1)

        return if (snapToPosition < 0 || snapToPosition >= itemCount) {
            RecyclerView.NO_POSITION
        } else {
            snapToPosition
        }
    }

    private fun isForwardFling(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Boolean {
        return if (layoutManager.canScrollHorizontally()) {
            velocityX > 0
        } else {
            velocityY > 0
        }
    }

    private fun isReverseLayout(layoutManager: RecyclerView.LayoutManager): Boolean {
        val itemCount = layoutManager.itemCount
        if (layoutManager is ScrollVectorProvider) {
            val vectorProvider = layoutManager as ScrollVectorProvider
            val vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1)
            if (vectorForEnd != null) {
                return vectorForEnd.x < 0 || vectorForEnd.y < 0
            }
        }
        return false
    }

    private fun getOrientationHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        return if (layoutManager.canScrollVertically()) {
            getVerticalHelper(layoutManager)
        } else if (layoutManager.canScrollHorizontally()) {
            getHorizontalHelper(layoutManager)
        } else {
            null
        }
    }
}