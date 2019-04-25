package com.zp.zphoto_lib.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.zp.zphoto_lib.R

class ZPhotoRVDivider : RecyclerView.ItemDecoration {

    private var paint: Paint? = null
    private var leftPadding = 0
    private var rightPadding = 0
    // 分割线高度
    private var dividerHeight = 1
    // 列表的方向
    private var orientation = HORIZONTAL

    companion object {
        const val VERTICAL = LinearLayoutManager.VERTICAL
        const val HORIZONTAL = LinearLayoutManager.HORIZONTAL

        /** 默认 颜色不是很突出的 分割线 */
        fun getDefaultDivider(context: Context) =
            ZPhotoRVDivider(
                context,
                HORIZONTAL,
                R.dimen.zphoto_default_divider_height,
                R.color.zphoto_rv_divider_color,
                R.dimen.zphoto_default_divider_padding,
                R.dimen.zphoto_default_divider_padding
            )

    }

    /**
     * 默认分割线
     * @param orientation 列表方向
     */
    constructor(context: Context, orientation: Int) {
        if (orientation != VERTICAL && orientation != HORIZONTAL) {
            throw IllegalArgumentException("请输入正确的参数！")
        }
        this.orientation = orientation
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint?.color = ContextCompat.getColor(context, R.color.zphoto_gray)
        paint?.style = Paint.Style.FILL
    }

    /**
     * 自定义分割线
     * @param orientation   列表方向
     * @param dividerHeight 分割线高度
     * @param dividerColor  分割线颜色
     */
    constructor(context: Context, orientation: Int, dividerHeight: Int, dividerColor: Int) : this(context, orientation) {
        if (dividerHeight != 0) {
            this.dividerHeight = context.resources.getDimension(dividerHeight).toInt()
        }
        if (this.dividerHeight == 0) this.dividerHeight = 1
        if (dividerColor != 0) {
            paint?.color = ContextCompat.getColor(context, dividerColor)
        }
    }

    /**
     * 自定义分割线
     * @param orientation   列表方向
     * @param dividerHeight 分割线高度
     * @param dividerColor  分割线颜色
     * @param leftPadding   左边距
     * @param rightPadding  右边距
     */
    constructor(context: Context, orientation: Int, dividerHeight: Int, dividerColor: Int, leftPadding: Int, rightPadding: Int)
            : this(context, orientation, dividerHeight, dividerColor) {
        if (leftPadding <= 0) {
            this.leftPadding = 0
        } else {
            this.leftPadding = context.resources.getDimension(leftPadding).toInt()
        }
        if (rightPadding <= 0) {
            this.rightPadding = 0
        } else {
            this.rightPadding = context.resources.getDimension(rightPadding).toInt()
        }
    }

    // 获取分割线尺寸
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 0, dividerHeight)
    }

    // 绘制分割线
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        if (orientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    // 绘制横向 item 分割线
    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.measuredWidth - parent.paddingRight
        val childSize = parent.childCount
        for (i in 0 until childSize) {
            val child = parent.getChildAt(i)
            val layoutParams = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + layoutParams.bottomMargin
            val bottom = top + dividerHeight
            drawLine(left, top, right, bottom, canvas)
        }
    }

    // 绘制纵向 item 分割线
    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.measuredHeight - parent.paddingBottom
        val childSize = parent.childCount
        for (i in 0 until childSize) {
            val child = parent.getChildAt(i)
            val layoutParams = child.layoutParams as RecyclerView.LayoutParams
            val left = child.right + layoutParams.rightMargin
            val right = left + dividerHeight
            drawLine(left, top, right, bottom, canvas)
        }
    }

    private fun drawLine(left: Int, top: Int, right: Int, bottom: Int, canvas: Canvas) {
        if (paint != null) {
            canvas.drawRect(
                left.toFloat() + leftPadding,
                top.toFloat(),
                right.toFloat() - rightPadding,
                bottom.toFloat(), paint
            )
        }
    }
}