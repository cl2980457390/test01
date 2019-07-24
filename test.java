public class ViewHolderOrderItem extends RecyclerViewHolder implements View.OnClickListener {
    private Context mContext;
    private int mType;
    private OrderItemModel mData;
    private LayoutInflater mInflater;
    private static final int MAX_DISPLAY_PRODUCTS_COUNT = 4;
    private static final int MAX_DISPLAY_PRODUCTS_NAME_COUNT = 3;

    public TextView itemtype;
    public TextView txtStoreTitle;
    public TextView txtOrderState;
//xx
    public ViewGroup listProductIcon;
    public ViewGroup listProductName;
    public TextView txtPriceSummary;
    public TextView txtTimePattern;

    public TextView txtOrderSummary;
    public TextView btnPayNow;
    public TextView btnCommentNow;
    public TextView btnBuyAgain;

    public TextView txtOrderfoodDetail;

    public ImageLoaderView mSellerImg;
    public TextView mSellerName;
    public TextView mCreateTime;
    public ImageView mIconAlarm;

    public LinearLayout mBtnContainer;
    public View footerContainer;

    public static final int ORDERLIST_REQUESTCODE = 0;
    public static final int REQUESTCODE_COMMENT = 22;


    public ViewHolderOrderItem(Context context, View itemView) {
        super(itemView);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        itemtype = (TextView) itemView.findViewById(R.id.ic_type);
        txtStoreTitle = (TextView) itemView.findViewById(R.id.txt_title);
        txtOrderState = (TextView) itemView.findViewById(R.id.txt_order_state);

        listProductIcon = (ViewGroup) itemView.findViewById(R.id.list_products_icon);
        listProductName = itemView.findViewById(R.id.list_products_name);
        txtPriceSummary = (TextView) itemView.findViewById(R.id.txt_price_summary);
        txtTimePattern = (TextView) itemView.findViewById(R.id.txt_delivery_pattern);

        txtOrderSummary = (TextView) itemView.findViewById(R.id.txt_order_summary);
        btnPayNow = (TextView) itemView.findViewById(R.id.btn_pay_now);
        btnCommentNow = (TextView) itemView.findViewById(R.id.btn_comment_now);
        btnBuyAgain = (TextView) itemView.findViewById(R.id.btn_buy_again);

        txtOrderfoodDetail = itemView.findViewById(R.id.txt_orderfood_detail);

        mSellerImg = itemView.findViewById(R.id.order_list_item_seller_img);
        mSellerName = itemView.findViewById(R.id.order_list_item_seller_name);
        mCreateTime = itemView.findViewById(R.id.order_list_item_create_time);
        mBtnContainer = itemView.findViewById(R.id.bottom_right_tips);
        footerContainer = itemView.findViewById(R.id.list_footer);
        mIconAlarm = itemView.findViewById(R.id.order_item_img_alarm);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mData != null && mData.ordersubtype != null && mData.ordersubtype.equals("food")) {
            UiUtil.startSchema(mContext, mData.detailaction);
        } else if (mData != null && mData.ordersubtype != null && mData.ordersubtype.equals("scancode")) {//扫码购
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (mData.status==OrderStatus.ORDER_STATE_PAY){
                intent.setClass(mContext, OrderDetailActivity.class);
                intent.putExtra(ExtraConstants.QR_FROM_ORDERLIST,  true);
            }else {
                intent.setClass(mContext, OrderDetailActivity.class);
            }
            intent.putExtra(ExtraConstants.EXTRA_ORDER_ID,  mData.id);
            intent.putExtra(EXTRA_BACK_TO_HOME,false);
            mContext.startActivity(intent);
        } else {
            if (mData != null && mData.id != null) {
                if (isFakePay(mData.id, mData.totalpayment)) return;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClass(mContext, OrderDetailActivity.class);
                intent.putExtra(EXTRA_ORDER_ID, mData.id);
                if (mContext instanceof Activity) {
                    ((Activity) mContext).startActivityForResult(intent, ORDERLIST_REQUESTCODE);
                } else {
                    mContext.startActivity(intent);
                }
            }
        }
    }

    public void setData(OrderItemModel data, int type) {
        if (data == null) {
            return;
        }
        mData = data;
        mType = type;
        refreshVendorInfo();
        refreshOrderStatus();
        refreshProductList();
        refreshAdditionalButton();
    }

    private void refreshVendorInfo() {
        if (mData == null) {
            return;
        }

        View.OnClickListener onVendorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtil.startSchema(mContext, mData.seller.action);
            }
        };

        if (itemtype != null) {

            itemtype.setText("["+mData.ordertypetag+"]");

        }

        if (mSellerName != null) {
            if (mData.seller != null && mData.seller.title != null && !mData.seller.title.isEmpty()) {
                mSellerName.setText(mData.seller.title);
                mSellerImg.setImageByUrl(mData.icon);
                if (mData.seller.action != null && !mData.seller.action.isEmpty()) {
                    mSellerName.setOnClickListener(onVendorClickListener);
                }
            } else {
                mSellerName.setText("");
            }
        }

        mCreateTime.setText(mContext.getString(R.string.order_list_order_place_time, formateOrderTime(mData.timeinfo.generate,"")));
        if (mData.products != null) {
            txtOrderfoodDetail.setText(mContext.getString(R.string.order_list_product_count, mData.products.size()));
        }
    }

    private void refreshOrderStatus() {
        if (mData == null) {
            return;
        }

        //重置描述
        mIconAlarm.setVisibility(View.GONE);
        txtOrderSummary.setText("");
        txtOrderState.setText(mData.statusmsg);

        String appointTime = null;
        if (txtOrderState != null && txtOrderSummary != null) {
            switch (mData.status) {
                case OrderStatus.ORDER_STATE_PAY:
                    if (mData.timeinfo != null) {
                        mIconAlarm.setVisibility(View.VISIBLE);
                        String dateStr = mContext.getString(R.string.order_list_order_pay_deadline,formateOrderTime(mData.timeinfo.payend,""));
                        SpannableString spannableString = new SpannableString(dateStr);
                        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(UiUtil.sp2px(mContext,14));
                        spannableString.setSpan(absoluteSizeSpan, 2, dateStr.length()-5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
                        spannableString.setSpan(redSpan, 2, dateStr.length()-5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        txtOrderSummary.setText(spannableString);
                    }
                    break;
                case OrderStatus.ORDER_STATE_PICK:


                    break;
                case OrderStatus.ORDER_STATE_TOSTORE:

                    break;
                case OrderStatus.ORDER_STATE_TODELIVER:

                    break;
                case OrderStatus.ORDER_STATE_DELIVER:


                    break;
                case OrderStatus.ORDER_STATE_PICKSELF:
                    if (mData.ordersubtype.equals("food")) {

                    } else if(mData.ordersubtype.equals("scancode")) {

                    }else {
                    }
                    if (mData.ordersubtype.equals("food")) {
                        if (mData.timeinfo != null) {

                        }
                    } else if(mData.ordersubtype.equals("scancode")){
                        if (mData.timeinfo != null) {


                        }
                    }else {
                        appointTime = formatExpectTime(mData.deliverymode == OrderItemModel.TOADY ? mData.texpecttime : mData.nexpecttime, mData.ispickself == 1);
                    }
                    break;
                case OrderStatus.ORDER_STATE_COMPLETE:


                    break;
                case OrderStatus.ORDER_STATE_CANCEL:

                    break;
                case OrderStatus.ORDER_STATE_REFUNDING:
                case OrderStatus.ORDER_STATE_RETURNING:

                    break;
                case OrderStatus.ORDER_STATE_REFUNDED:
                case OrderStatus.ORDER_STATE_SPLITTED:

                    break;
                case OrderStatus.ORDER_STATE_GROUPBUYING:
                    break;
                case OrderStatus.ORDER_STATE_WAITING_PROCESS:
                    if (mData.timeinfo != null) {

                    }
                    break;
                case OrderStatus.ORDER_STATE_PROCESSING:
                    if (mData.timeinfo != null) {

                    }
                    break;
                default:

                    break;
            }

            if (!TextUtils.isEmpty(txtOrderSummary.getText())) {
                footerContainer.setVisibility(View.VISIBLE);
            }

        }
    }

    private String formateOrderTime(long time, String timeTag) {
        String date = "";
        if (time <= 0) return "";
        if (UiUtil.isCurrentYear(time)) {
            date = UiUtil.msecToFromatDate(time, "MM-dd HH:mm");
        } else {
            date = UiUtil.msecToFromatDate(time, "YY-MM-dd HH:mm");
        }
        StringBuilder sb = new StringBuilder();
        return sb.append(timeTag).append(" ").append(date).toString();
    }


    private String formatExpectTime(DeliverTimeModel data, String timeTag) {
        if (data == null) {
            return null;
        }


        String date = "";
        if (data.date > 0) {
            date = UiUtil.msecToFromatDate(data.date, "MM-dd");
        }
        String time = "";
        if (data.timeslots != null && data.timeslots.size() > 0) {
            DeliverSlot slot = data.timeslots.get(0);
            if (slot != null && slot.from != null && !slot.from.isEmpty() && slot.to != null && !slot.to.isEmpty()) {
                time = slot.from + "-" + slot.to;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(timeTag);
        sb.append(date).append(" ").append(time);
        return sb.toString();
    }


    private String formatExpectTime(DeliverTimeModel data, boolean pickSelf) {
        StringBuilder sb = new StringBuilder();
        if (pickSelf) {
            sb.append(mContext.getString(R.string.order_list_appointed_pickup));
        } else {
            sb.append(mContext.getString(R.string.order_list_appointed_deliver));
        }
        return formatExpectTime(data, sb.toString());
    }

    private void refreshProductList() {
        if (mData == null) {
            return;
        }

        if (mData.products != null) {

            if (OrderItemModel.ORDER_SUB_TYPE_SCANCODE.equals(mData.ordersubtype) || OrderItemModel.ORDER_SUB_TYPE_YHSHOP.equals(mData.ordersubtype)) {

                int childCount = listProductName.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    listProductName.getChildAt(i).setVisibility(View.GONE);
                }

                listProductName.setVisibility(View.VISIBLE);
                listProductIcon.setVisibility(View.GONE);

                Iterator it = mData.products.iterator();
                int i = 0;
                while (it.hasNext()) {
                    if (i >= MAX_DISPLAY_PRODUCTS_NAME_COUNT) {
                        break;
                    }
                    ProductsDataBean product = (ProductsDataBean) it.next();
                    View productView = listProductName.getChildAt(i);
                    if (productView == null) {
                        productView = mInflater.inflate(R.layout.order_list_product_name_item, null, false);
                        listProductName.addView(productView);
                    } else {
                        productView.setVisibility(View.VISIBLE);
                    }

                    TextView mNameTv = productView.findViewById(R.id.order_list_item_product_name);
                    TextView mCountTv = productView.findViewById(R.id.order_list_item_product_count);
                    if (i == MAX_DISPLAY_PRODUCTS_NAME_COUNT-1 && (mData.products.size() > MAX_DISPLAY_PRODUCTS_NAME_COUNT)) {
                        mNameTv.setText("···");
                        mCountTv.setText("");
                    } else {
                        mNameTv.setText(product.title);
                        mCountTv.setText("x" +(int)product.num/100);
                    }

                    i++;
                }

            } else if (listProductIcon != null) {
                int childCount = listProductIcon.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    listProductIcon.getChildAt(i).setVisibility(View.GONE);
                }

                listProductName.setVisibility(View.GONE);
                listProductIcon.setVisibility(View.VISIBLE);

                Iterator it = mData.products.iterator();
                int i = 0;
                while (it.hasNext()) {
                    if (i > MAX_DISPLAY_PRODUCTS_COUNT) {
                        break;
                    }
                    ProductsDataBean product = (ProductsDataBean) it.next();
                    View productView = listProductIcon.getChildAt(i);
                    if (productView == null) {
                        if (i == MAX_DISPLAY_PRODUCTS_COUNT) {
                            productView = new TextView(mContext);
                            TextView textView = (TextView) productView;
                            textView.setLines(1);
                            //vivo某些手机上显示不下，所以把字号调小点
                            textView.setTextSize(8);
                            textView.setText("···");
                        } else {
                            productView = mInflater.inflate(R.layout.order_list_product_item, null, false);
                        }
                        listProductIcon.addView(productView);
                    } else {
                        productView.setVisibility(View.VISIBLE);
                    }

                    if (i < MAX_DISPLAY_PRODUCTS_COUNT) {
                        ImageLoaderView img = productView.findViewById(R.id.img_icon);
                        img.setImageByUrl(product.imgurl);
                    }

                    i++;
                }

                if (mData.ordersubtype.equals("food")) {
                    txtOrderfoodDetail.setVisibility(View.VISIBLE);
                    txtOrderfoodDetail.setText(mData.title);
                } else if (mData.ordersubtype.equals("scancode")) {
                    txtOrderfoodDetail.setVisibility(View.GONE);
                    if (txtTimePattern != null) {
                        txtTimePattern.setText("");
                    }
                } else {
//                txtOrderfoodDetail.setVisibility(View.GONE);
                    if (txtTimePattern != null) {
                        if (mData.status == OrderStatus.ORDER_STATE_PAY || mData.status == OrderStatus.ORDER_STATE_CANCEL) {
                            txtTimePattern.setVisibility(View.GONE);
                        } else if (mData.deliverymode == 1) {
                            txtTimePattern.setText(mContext.getString(R.string.order_products_pattern_t));
                            txtTimePattern.setVisibility(View.GONE);
                        } else if (mData.deliverymode == 2 || mData.deliverymode == 4) {
                            txtTimePattern.setText(mContext.getString(R.string.order_products_pattern_n));
                            txtTimePattern.setVisibility(View.GONE);
                        } else {
                            txtTimePattern.setVisibility(View.GONE);
                        }
                    }
                }


            }
            if (txtPriceSummary != null) {
                txtPriceSummary.setText(UiUtil.centToYuanString(mContext, mData.totalamount));
            }
        }
    }

    private void refreshAdditionalButton() {
        if (mData == null) {
            return;
        }

        btnPayNow.setVisibility(View.GONE);
        btnBuyAgain.setVisibility(View.GONE);
        btnCommentNow.setVisibility(View.GONE);

        if (mData.actioninfos != null && mData.actioninfos.size()>0) {

            footerContainer.setVisibility(View.VISIBLE);
            for (int i = 0; i < mData.actioninfos.size(); i++) {
                final OrderItemActionInfo actionInfo = mData.actioninfos.get(i);
                TextView btn = (TextView) mBtnContainer.getChildAt(i);
                if (btn != null) {
                    btn.setText(actionInfo.getActionname());
                    if (actionInfo.getHighlight() == 1) {
                        btn.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_order_item_btn_hilight));
                        btn.setTextColor(ContextCompat.getColor(mContext, R.color.base_color));
                    } else {
                        btn.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_order_item_btn_normal));
                        btn.setTextColor(ContextCompat.getColor(mContext, R.color.black_a87));
                    }
                    if (actionInfo.getActiontype() == OrderItemActionInfo.CREATOR.getACTION_PAY()) {
                        btn.setOnClickListener(mPayNowClickListener);
                        btn.setVisibility(View.VISIBLE);
                    } else if (actionInfo.getActiontype() == OrderItemActionInfo.CREATOR.getACTION_COMMENT()) {
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //评价享积分改为跳转action
                                UiUtil.startSchema(mContext, actionInfo.getActionurl());
                                trackCommentClick();
                            }
                        });
                        //根据actiontype = 2 且 actionurl 不为空 展示 评价享积分
                        btn.setVisibility(!TextUtils.isEmpty(actionInfo.getActionurl()) ? View.VISIBLE : View.GONE);
                    } else if (actionInfo.getActiontype() == OrderItemActionInfo.CREATOR.getACTION_BUY_AGAIN()) {
                        btn.setOnClickListener(mBuyAgainClickListener);
                        btn.setVisibility(View.VISIBLE);
                    } else if (!TextUtils.isEmpty(actionInfo.getActionurl())) {
                        UiUtil.startSchema(mContext, actionInfo.getActionurl());
                        btn.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else if (TextUtils.isEmpty(txtOrderSummary.getText())) {
            footerContainer.setVisibility(View.GONE);
        }


    }

    private View.OnClickListener mPayNowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mData == null) {
                return;
            }

            ConfirmPayInfoModel payinfo = new ConfirmPayInfoModel();
            payinfo.orderid = mData.id;
            payinfo.paymodes = mData.paychoose;
            payinfo.payprice = UiUtil.centToYuanString(mContext, mData.totalpayment);
            payinfo.desc = mData.desc;
            payinfo.totalbalance = mData.totalbalance;
            payinfo.balancepay = mData.balancepay;
            payinfo.isPickSelf = mData.ispickself == 1;
            payinfo.ordersubtype=mData.ordersubtype;

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(mContext, PayActivity.class);
            intent.putExtra(PRE_ORDER_INFO, payinfo);

            if (mContext instanceof Activity) {
                ((Activity) mContext).startActivityForResult(intent, ORDERLIST_REQUESTCODE);
            } else {
                mContext.startActivity(intent);
            }
            trackClickOrderSubmit();
        }
    };

    //立即支付埋点
    private void trackClickOrderSubmit() {

    }

    //曝光埋点
    public void trackExpo() {
        if (mData.actioninfos == null || mData.actioninfos.isEmpty()) {
            return;
        }
        for (OrderItemActionInfo actionInfo : mData.actioninfos) {
            if (actionInfo == null || TextUtils.isEmpty(actionInfo.getActionname())) {
                continue;
            }
            trackButtonExpo(actionInfo.get_uuid());
        }
    }

    private void trackButtonExpo(String _uuid_){

    }

    private View.OnClickListener mCommentNowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mData == null) {
                return;
            }
            ArrayMap<String, Object> arrayMap = new ArrayMap<>();
            arrayMap.put(BUNDLE_ORDER_ID, mData.id);
            if (mContext instanceof Activity) {
                NavgationUtil.INSTANCE.startActivityForResultOnJava((Activity) mContext, BundleUri.ACTIVITY_PUBLISHCOMMENT, arrayMap, REQUESTCODE_COMMENT);
            } else {
                NavgationUtil.INSTANCE.startActivityOnJava(mContext, BundleUri.ACTIVITY_PUBLISHCOMMENT, arrayMap);
            }
            BuriedPointUtil.getInstance().buttonClickTrack(mContext.getResources().getString(R.string.track_order_list_comment_now));
        }
    };

    private View.OnClickListener mBuyAgainClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mData == null) {
                return;
            }


            trackBuyAgainClick();
        }
    };

    /**
     * 再次购买点击埋点
     */
    private void trackBuyAgainClick() {
        CartArgsModel cartArgsModel = new CartArgsModel();
        if (mData.seller != null) {
            cartArgsModel.sellerId = mData.seller.id;
        }
        cartArgsModel.buyAgainProducts = mData.products;


        ArrayMap<String, Object> arrayMap = new ArrayMap<>();
        arrayMap.put(ExtraConstants.CART_ARGS_MODEL, cartArgsModel);
        NavgationUtil.INSTANCE.startActivityOnJava(mContext, BundleUri.ACTIVITY_SELLERCART, arrayMap);
    }

    /**
     * 评价享积分点击埋点
     */
    private void trackCommentClick() {

    }

    /**
     * 将商品id拼接起来用于埋点
     * @return
     */
    private String convertTrackProductId() {
        StringBuilder productId = new StringBuilder();
        for (int i = 0; i < mData.products.size(); i++){
            productId.append(mData.products.get(i).id);
            if (i != mData.products.size() - 1){
                productId.append(",");
            }
        }
        return productId.toString();
    }

    private String convertTrackProductnum() {
        StringBuilder productcount = new StringBuilder();
        for (int i = 0; i < mData.products.size(); i++){
            productcount.append(((int) mData.products.get(i).num));
            if (i != mData.products.size() - 1){
                productcount.append(",");
            }
        }
        return productcount.toString();
    }

    private String convertTrackActionName() {
        if (mData.actioninfos == null || mData.actioninfos.isEmpty()) return "-99";
        StringBuilder actionName = new StringBuilder();
        for (OrderItemActionInfo actionInfo : mData.actioninfos) {
            if (actionInfo == null || TextUtils.isEmpty(actionInfo.getActionname())) continue;
            actionName.append(actionInfo.getActionname()).append(",");
        }
        return actionName.toString();
    }

    public boolean hasActionButtons() {
        if (mData.actioninfos == null || mData.actioninfos.isEmpty()) return false;
        return true;
    }

    private boolean isFakePay(String id, int totalPayment) {
        if (YHSession.getSession().hasAttribute(SessionKey.IS_FAKE_PAY) && (Boolean) YHSession.getSession().getAttribute(SessionKey.IS_FAKE_PAY)) {
            YHSession.getSession().putAttribute(SessionKey.IS_FAKE_PAY, false);
            BusUtil.INSTANCE.register(this);
            return true;
        }
        return false;
    }
}
