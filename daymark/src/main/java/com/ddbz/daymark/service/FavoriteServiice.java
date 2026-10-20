package com.ddbz.daymark.service;

import com.ddbz.daymark.pojo.vo.function.FavoriteVO;

import java.util.List;

public interface FavoriteServiice {

    public void Favorite(int postId);

    public void DeleteFavorite(int postId);

    public Boolean IsFavorite(int postId);

    public List<FavoriteVO> FavoriteInformation(int postId);
}
