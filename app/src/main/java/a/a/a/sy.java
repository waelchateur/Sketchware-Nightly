package a.a.a;

import com.besome.sketch.beans.ViewBean;

// todo: move this to another package
// 'sy' is used in ViewPane items, example ItemLinearLayout
public interface sy {
    ViewBean getBean();

    boolean getFixed();

    void setBean(ViewBean viewBean);

    void setFixed(boolean fixed);

    void setSelection(boolean selection);
}
