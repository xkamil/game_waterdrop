package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

public class GdxGame1 extends ApplicationAdapter {

  private SpriteBatch batch;

  private Texture dropImg;
  private Array<Rectangle> drops;
  private long lastDropTime;

  private Texture bucketImg;
  private Rectangle bucket;

  private Music rainMusic;
  private Sound dropSound;
  private OrthographicCamera camera;

  private BitmapFont font;
  private int dropSpeed = 200;

  private int score;

  private Texture backgroundImg;

  @Override
  public void create() {
    batch = new SpriteBatch();

    score = 0;
    font = new BitmapFont();
    font.getData().setScale(2);

    backgroundImg = new Texture("background.jpg");

    drops = new Array<>();
    dropImg = new Texture("drop.png");

    bucketImg = new Texture("bucket.png");
    bucket = new Rectangle();
    bucket.x = 800 / 2 - 64 / 2;
    bucket.y = 20;
    bucket.width = dropImg.getWidth();
    bucket.height = dropImg.getHeight();

    rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
    dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));

    camera = new OrthographicCamera();
    camera.setToOrtho(false, 800, 480);
  }

  @Override
  public void render() {
    ScreenUtils.clear(1, 0, 0, 1);
    camera.update();

    if (TimeUtils.nanoTime() - lastDropTime > 1000000000) {
      spawnRainDrop();
    }

    for (Iterator<Rectangle> iter = drops.iterator(); iter.hasNext(); ) {
      Rectangle raindrop = iter.next();
      raindrop.y -= dropSpeed * Gdx.graphics.getDeltaTime();
      if (raindrop.y + 64 < 0) {
        iter.remove();
      }
      if (raindrop.overlaps(bucket)) {
        dropSound.play();
        score++;
        dropSpeed += 10;
        iter.remove();
      }
    }

    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    batch.draw(backgroundImg, 0, 0, 800, 480);
    batch.draw(bucketImg, bucket.x, bucket.y);
    font.draw(batch, "Score: " + score, 10, 480 - 10);
    drops.forEach(drop -> batch.draw(dropImg, drop.x, drop.y));
    batch.end();

    rainMusic.setLooping(true);
    rainMusic.play();

    if (Gdx.input.isTouched()) {
      Vector3 touchPos = new Vector3();
      touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
      camera.unproject(touchPos);
      bucket.setX(touchPos.x - bucketImg.getWidth() / 2);
    }

    if (Gdx.input.isKeyPressed(Keys.LEFT)) {
      bucket.setX(bucket.getX() - 200 * Gdx.graphics.getDeltaTime());
    }

    if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
      bucket.setX(bucket.getX() + 200 * Gdx.graphics.getDeltaTime());
    }

    if (bucket.x < 0) {
      bucket.x = 0;
    }
    if (bucket.x > 800 - 64) {
      bucket.x = 800 - 64;
    }
  }

  @Override
  public void dispose() {
    batch.dispose();
    dropImg.dispose();
    bucketImg.dispose();
    dropSound.dispose();
    rainMusic.dispose();
    batch.dispose();
  }

  private void spawnRainDrop() {
    Rectangle raindrop = new Rectangle();
    raindrop.x = MathUtils.random(0, 800 - 64);
    raindrop.y = 480;
    raindrop.width = 64;
    raindrop.height = 64;
    drops.add(raindrop);
    lastDropTime = TimeUtils.nanoTime();
  }
}
