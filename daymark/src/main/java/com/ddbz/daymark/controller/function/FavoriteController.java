package com.ddbz.daymark.controller.function;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ddbz.daymark.common.Result;
import com.ddbz.daymark.pojo.entity.function.FavoriteEntity;
import com.ddbz.daymark.pojo.vo.function.FavoriteVO;
import com.ddbz.daymark.service.FavoriteServiice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("favorite")
@RestController
public class FavoriteController {

    @Autowired
    FavoriteServiice favoriteService;

    @PutMapping
    public Result<Object> favorite(@RequestParam int postId){
        log.info("开始进行收藏操作");
        favoriteService.Favorite(postId);
        return Result.success();
    }

    @DeleteMapping
    public Result<Object> deleteFavorite(@RequestParam int postId){
        log.info("开始进行取消收藏操作");
        favoriteService.DeleteFavorite(postId);
        return Result.success();
    }

    @GetMapping("isFavorite")
    public Result<Boolean> isFavorite(@RequestParam int postId){
        log.info("判断用户是否收藏该帖子");
        return Result.success(favoriteService.IsFavorite(postId));
    }

    @GetMapping("favoriteInformation")
    public Result<List<FavoriteVO>> favoriteInformation(@RequestParam int postId){
        log.info("获取帖子的所有收藏信息");
        return Result.success(favoriteService.FavoriteInformation(postId));
    }
}
