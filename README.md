<html>
    <h3>OpenGLES</h3>
    <p>完成OpenGLES的基础内容，使用shader, light等操作，但是目前来看手机好像不支持3.0的版本。</p>
    <h3>GraphicsArchitecture</h3>
    <p>Surface, Surface Holder, EGLSurface, SurfaceView, GLSurfaceView,
    SurfaceTexture, TextureView, SurfaceFlinger, and Vulkan.</p>
    <h3>SoundRecorder</h3>
    <p>录音机应用，继承MediaRecorder类，底层系统已经封装好AMR, 3GPP, AAC, WAV格式。</p>
    <hr>
    <h3>RuntimePermissions</h3>
    <p>写完ContactsFragment部分，访问Contacts数据库。</p>
    <p>权限配置，引入Camera, Contacts模块。</p>
    <p>引入CameraView框架进行显示。</p>
    <hr>
    <h3>Grafika</h3>
    <p>和MediaCodec相关，详细学习编解码。</p>
    <p>写MediaCodec的测试类，OpenGL绘制视频，encoder进行编码，MediaMuxer保存为mp4格式。</p>
    <hr>
    <h3>ExoPlayer</h3>
    <p>音视频播放框架，容易拓展。</p>
    <hr>
    <h3>UniversalMusicPlayer</h3>
    <p>安卓音乐播放框架使用。</p>
    <p>MediaBrowser MediaController MediaSession</p>
    <p>从BaseActivity创建MediaBrowserCompat时传递MusicService，创建本地服务，该服务继承
    MediaBrowserServiceCompat，在实现onLoadChildren函数的过程中加载MediaProvider提供的
    数据，MediaBrowserFragment用于显示，PlaybackControlsFragment用于播放控制。</p>
    <p>LocalPlayback实现Playback进行具体的播放逻辑，PlaybackManager管理播放，是Playback和
    MusicService沟通的桥梁，QueueManager提供QueueItem数据。</p>
    <hr>
    <h3>MusicFX</h3>
    <p>音乐播放，音效控制。</p>
    <p>通过Virtualizer (虚拟化), BassBoost (低音增强), Equalizer (均衡器),
    PresetRevert (回音)来调节音效，目前还需要传进来AudioSession才能生效。</p>
    <p>从startService服务改成bindService服务。</p>
    <p>目前已经修改完成，play, pause, rewind, skip, stop, uri六个按钮的功能都正常，但是不大好后台播放。
    目前是按home键后台播放，如果按back键则是退出播放，用户按back键的意图不好推测。</p>
    <p>重新修改架构，将播放控制和音乐列表放在一个fragment，将MediaEffect放在另外一个fragment。
    点击ActionBar上面的按钮切换到MediaEffect界面调整参数。</p>
    <p>增加音乐列表，传递AudioSession参数。</p>
</html>
