package com.ithema.reggie;

import com.ithema.reggie.entity.Dish;
import com.ithema.reggie.mapper.DishMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 18:10
 */
@SpringBootTest
public class UploadTest {

    @Resource
    private DishMapper dishMapper;

    @Test
    public void uploadTest() {
        String img = "abc.jpg";
        String suffix = img.substring(img.lastIndexOf("."));
        System.out.println(suffix);// .jpg
    }

    @Test
    public void selectDishAndCategoryNameTest() {
        List<Dish> dishes = dishMapper.selectDishAndCategoryName();
        for (Dish dish : dishes) {
            System.out.println("dish=" + dish);
        }

    }

    @Test
    public void test() {
        int[] a = new int[]{4,5,3,6,7};
        int i = 0,j=a.length - 1,temp;
        while (i < j) {
            while (a[i]%2 != 0) ++i;
            while (a[j]%2 == 0) --j;
            if (i < j) {
                temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }
        for (int k = 0; k < a.length; k++) {
            System.out.print(a[k] + " ");
        }
    }
}
