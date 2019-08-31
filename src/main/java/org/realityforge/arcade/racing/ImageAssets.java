package org.realityforge.arcade.racing;

import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLImageElement;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class ImageAssets
{
  @Nonnull
  private final Runnable _onReady;
  @Nonnull
  private final HTMLImageElement _carImage;
  @Nonnull
  private final HTMLImageElement _roadImage;
  @Nonnull
  private final HTMLImageElement _wallImage;
  private int _imagesLoading = 3;

  ImageAssets( @Nonnull final Runnable onReady )
  {
    _onReady = Objects.requireNonNull( onReady );
    _carImage = (HTMLImageElement) DomGlobal.document.createElement( "img" );
    _roadImage = (HTMLImageElement) DomGlobal.document.createElement( "img" );
    _wallImage = (HTMLImageElement) DomGlobal.document.createElement( "img" );

    _carImage.onload = this::onImageLoaded;
    _roadImage.onload = this::onImageLoaded;
    _wallImage.onload = this::onImageLoaded;

    _carImage.src = "car.png";
    _roadImage.src = "track_road.png";
    _wallImage.src = "track_wall.png";
  }

  @Nonnull
  HTMLImageElement getCarImage()
  {
    return _carImage;
  }

  @Nonnull
  HTMLImageElement getRoadImage()
  {
    return _roadImage;
  }

  @Nonnull
  HTMLImageElement getWallImage()
  {
    return _wallImage;
  }

  @Nullable
  private Object onImageLoaded( @Nonnull final Event event )
  {
    _imagesLoading--;
    if ( 0 == _imagesLoading )
    {
      _onReady.run();
    }
    return null;
  }
}
