package org.realityforge.arcade.adventure;

import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLImageElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class ImageAssets
{
  @Nonnull
  private final Set<String> _imageNames = new HashSet<>();
  @Nonnull
  private final Map<String, HTMLImageElement> _images = new HashMap<>();
  @Nonnull
  private final Runnable _onReady;
  private int _loadedImageCount;

  ImageAssets( @Nonnull final Runnable onReady )
  {
    _onReady = Objects.requireNonNull( onReady );
    _imageNames.add( "car" );
    _imageNames.add( "player2car" );
    _imageNames.add( "track_road" );
    _imageNames.add( "track_wall" );
    _imageNames.add( "track_flag" );
    _imageNames.add( "track_goal" );
    _imageNames.add( "track_tree" );
    for ( final String imageName : _imageNames )
    {
      final HTMLImageElement img = (HTMLImageElement) DomGlobal.document.createElement( "img" );
      img.onload = this::onImageLoaded;
      img.src = imageName + ".png";
      _images.put( imageName, img );
    }
  }

  @Nullable
  private Object onImageLoaded( @Nonnull final Event event )
  {
    _loadedImageCount++;
    if ( _imageNames.size() == _loadedImageCount )
    {
      _onReady.run();
    }
    return null;
  }

  @Nonnull
  HTMLImageElement getImageByName( @Nonnull final String name )
  {
    return Objects.requireNonNull( _images.get( name ) );
  }
}
