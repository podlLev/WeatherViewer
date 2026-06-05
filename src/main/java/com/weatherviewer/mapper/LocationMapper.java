package com.weatherviewer.mapper;

import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.model.Location;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public abstract class LocationMapper {

    public abstract Location fromDto(LocationDto locationDto);

}
