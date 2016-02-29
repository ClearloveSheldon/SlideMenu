#SlideMenu

春节的时候在家里完成一个二手交易项目的时候，需求要求做一个侧滑菜单，菜单的形式就跟QQ差不多，效果上跟QQ略有一点差别。QQ在菜单滑动的时候，菜单部分的View两边都有一个隐藏的效果;我们这个App仅仅需要实现最简单的侧滑菜单效果即可，实现复杂程度上，较QQ小了许多。

以前都是项目中有侧滑菜单，我都是拿现成的开源项目SlidingMenu来实现的。放假在家，时间比较充裕，就干脆自己实现了一波。

先上演示图(是用360手机助手实时演示的，所以效果上有些卡顿。在真机上运行很流畅。)
![](http://ac-owahavgd.clouddn.com/94dbe5902cd333d2.gif)

再看看QQ的侧滑菜单演示图

![](http://ac-owahavgd.clouddn.com/5a87eee55d4d66cf.gif)

QQ菜单在滑动的时候菜单View两端同时隐藏或滑出

##实现思路

1. 对于菜单的横向滑动，我们可以借助Android提供的HorizontalScrollView帮助我们实现。我们先通过继承HorizontalScrollView帮助我们创建一个可以横向滚动的视图。

2. 对于自定义控件，特别是自定义灵活的组合控件，一般要实现一个构造器
    ```
        public class SlideMenu extends HorizontalScrollView {
             public SlideMenu(Context context, AttributeSet attrs){
                super(context, attrs);
             }
        }
         第二个参数是一个属性集合，通过它我们能获得一些自定义的属性
    ```

3. 在values文件夹下建立一个attrs.xml文件，编写我们需要的自定义属性

    在实际项目中，我们一般需要设置菜单View的宽度，菜单以及内容View是使用哪一个布局，所以一般建立以下这三个属性就可以了。有其他需求的也可以自己扩展。

    ```
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <declare-styleable name="SlideMenu">
                <attr name="menu_layout" format="reference"></attr>
                <attr name="content_layout" format="reference"></attr>
                <attr name="menu_width" format="reference|dimension"></attr>
            </declare-styleable>
        </resources>
    ```

4. 在SlideMenu类中，我们可以创建一个init()方法，来帮我们完成解析自定义属性的值，并做一些其他初始化参数的操作。

    ```
        private void init(AttributeSet attrs) {
            //解析自定义参数的值
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SlideMenu);
            int menuLayoutId = typedArray.getResourceId(0, -1);
            int contentLayoutId = typedArray.getResourceId(1, -1);
            mMenuWidth = typedArray.getDimensionPixelOffset(2, 0);

            //加载View
            mMenu = View.inflate(getContext(), menuLayoutId, null);
            mContent = View.inflate(getContext(), contentLayoutId, null);

            //获取屏幕宽高
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            mScreenWidth = displayMetrics.widthPixels;
            mScreenHeight = displayMetrics.heightPixels;

            //设置为不能滑出边界(滑出边界的效果比较难看)
            setOverScrollMode(OVER_SCROLL_NEVER);

            //设置无滚动条
            setHorizontalScrollBarEnabled(false);
        }
    ```

5. 自定义ViewGroup通常要实现两个方法onMeasure 和 onLayout方法
    onMeasure:计算所有ChildView的宽度和高度 然后根据ChildView的计算结果，设置自己的宽和高 
    onLayout :对子View进行布局，会依次调用子ViewGroup的layout方法

    ```
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            //设置测量的宽高为屏幕的宽高
            setMeasuredDimension(
                    mScreenWidth, mScreenHeight);
            if(!mHasInit){
                //由于会多次测量，所以使用mHasInit保证布局只加载一次
                mHasInit = true;
                //创建子View，并加到ScrollView中
                mWrapper = new LinearLayout(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mMenuWidth+mScreenWidth,getMeasuredHeight());
                mWrapper.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(mMenuWidth, mScreenHeight);
                mWrapper.addView(mMenu, menuParams);
                LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(mScreenWidth, mScreenHeight);
                mWrapper.addView(mContent, contentParams);
                mWrapper.setLayoutParams(layoutParams);
                addView(mWrapper);
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            //change表示view有新的布局和尺寸。在子view被初始化并加载到父view的时候就会调用onLayout方法，并且change为true
            if(changed){
                //将ScrollView滚动到mMenuWidth的位置，也就是滚动到跟内容View的左侧对其，让菜单栏刚好完全隐藏在屏幕左侧
                //注意滚动的时机，当view有新的布局和尺寸时调用,也就是在初始化的时候去滚动。这个时候滚动才不会在打开页面时有滚动的效果
                scrollTo(mMenuWidth, 0);
            }
        }

    ```

6. 这样就基本实现了能够滑动的布局。接下来我们要处理触摸事件，以满足以下要求：
    * 当Scroller滚动到0位置时，可以响应任何事件
    * 当Scroller滚动到mMenuWidth位置时，只有触摸事件的X坐标小于50dp(大小任意设置)时,才能响应横向滑动事件。
    * 当Scroller滚动到0的位置时，点击事件的X坐标大于mMenuWidth时，使菜单View隐藏掉
    * 当手指抬起时Scroller滚动到小于mMenuWidth/2位置时，使Scroller平缓滚动到0,当手指抬起时Scroller滚动到大于mMenuWidth/2位置时，使Scroller平缓滚动到mMenuWidth
    
    如果了解了View的事件分发机制，对触摸事件的处理写起来就没有难度，只需要不断的调试就能实现效果，在这里就不贴代码了。


##基本使用方法

```
    <com.opensource.slidemenu.SlideMenu
        android:id="@+id/menu"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:menu_layout="@layout/menu"
        app:content_layout="@layout/content"
        app:menu_width="300dp"
        >
    </com.opensource.slidemenu.SlideMenu>
```

在@layout/menu和@layout/content中写布局或者直接引入Fragment都可以，灵活度比较高。


##QQ侧滑的思考
虽然没有动手去实现QQ的侧滑，但我也对QQ的侧滑菜单实现方法做了一些思考。

由于QQ菜单在滑动的时候会出现菜单View被压再内容View下面的情况，所以容器肯定不能使用LinearLayout，View叠加的页面我们就需要使用FrameLayout。

侧滑的效果可以这样实现：

   我们可以处理触摸事件，在横向滑动的时候，计算滑动距离，根据滑动距离计算此时
菜单View和内容View的距离屏幕最左边的值，然后动态改变菜单View和内容View的margin值。

