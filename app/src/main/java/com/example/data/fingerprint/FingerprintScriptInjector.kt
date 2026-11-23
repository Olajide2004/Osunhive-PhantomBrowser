package com.example.data.fingerprint

import com.example.data.local.entity.ProfileEntity
import org.json.JSONObject

object FingerprintScriptInjector {

  fun buildInjectionScript(profile: ProfileEntity): String {
    val platform = FingerprintGenerator.getPlatformForOs(profile.os)
    val userAgentSafe = JSONObject.quote(profile.userAgent)
    val platformSafe = JSONObject.quote(platform)
    val languagesArray = profile.languages.split(",").map { it.substringBefore(";").trim() }
    val languagesJson = "[" + languagesArray.joinToString(",") { JSONObject.quote(it) } + "]"
    val primaryLang = JSONObject.quote(languagesArray.firstOrNull() ?: "en-US")
    
    val webGlVendor = JSONObject.quote(profile.webGlVendor)
    val webGlRenderer = JSONObject.quote(profile.webGlRenderer)
    val timezoneSafe = JSONObject.quote(if (profile.timezone == "Based on IP") "UTC" else profile.timezone)
    
    val profileNameSafe = JSONObject.quote(profile.name)
    val vCamNameSafe = JSONObject.quote(profile.virtualCameraName)
    val vCamModeSafe = JSONObject.quote(profile.virtualCameraMode)
    val vCamMediaType = JSONObject.quote(profile.virtualCameraMediaType)
    val vCamMediaBase64 = JSONObject.quote(profile.virtualCameraMediaBase64 ?: "")
    val vCamEnabled = profile.virtualCameraEnabled

    return """
      (function() {
        'use strict';
        if (window.__phantom_injected__) return;
        window.__phantom_injected__ = true;

        function safeDefine(target, prop, getter) {
          if (!target) return;
          try {
            Object.defineProperty(target, prop, {
              get: getter,
              configurable: true,
              enumerable: true
            });
          } catch(e) {
            try { target[prop] = getter(); } catch(e2) {}
          }
        }

        try {
          // 1. Navigator overrides
          if (typeof navigator !== 'undefined') {
            safeDefine(navigator, 'userAgent', () => $userAgentSafe);
            safeDefine(navigator, 'appVersion', () => $userAgentSafe);
            safeDefine(navigator, 'platform', () => $platformSafe);
            safeDefine(navigator, 'hardwareConcurrency', () => ${profile.cpuThreads});
            safeDefine(navigator, 'deviceMemory', () => ${profile.deviceMemoryGb});
            safeDefine(navigator, 'languages', () => $languagesJson);
            safeDefine(navigator, 'language', () => $primaryLang);
            safeDefine(navigator, 'webdriver', () => undefined);

            if (typeof Navigator !== 'undefined' && Navigator.prototype) {
              safeDefine(Navigator.prototype, 'userAgent', () => $userAgentSafe);
              safeDefine(Navigator.prototype, 'appVersion', () => $userAgentSafe);
              safeDefine(Navigator.prototype, 'platform', () => $platformSafe);
              safeDefine(Navigator.prototype, 'hardwareConcurrency', () => ${profile.cpuThreads});
              safeDefine(Navigator.prototype, 'deviceMemory', () => ${profile.deviceMemoryGb});
              safeDefine(Navigator.prototype, 'languages', () => $languagesJson);
              safeDefine(Navigator.prototype, 'language', () => $primaryLang);
            }
          }

          // 2. Screen overrides
          if (typeof screen !== 'undefined') {
            const spoofW = ${profile.resolutionWidth};
            const spoofH = ${profile.resolutionHeight};
            safeDefine(screen, 'width', () => spoofW);
            safeDefine(screen, 'height', () => spoofH);
            safeDefine(screen, 'availWidth', () => spoofW);
            safeDefine(screen, 'availHeight', () => Math.max(spoofH - 40, 200));
            safeDefine(screen, 'colorDepth', () => 24);
            safeDefine(screen, 'pixelDepth', () => 24);

            if (typeof Screen !== 'undefined' && Screen.prototype) {
              safeDefine(Screen.prototype, 'width', () => spoofW);
              safeDefine(Screen.prototype, 'height', () => spoofH);
              safeDefine(Screen.prototype, 'availWidth', () => spoofW);
              safeDefine(Screen.prototype, 'availHeight', () => Math.max(spoofH - 40, 200));
            }
          }

          // 3. WebGL Vendor & Renderer Spoofing
          ${if (profile.webGlSpoofing) """
          try {
            if (typeof WebGLRenderingContext !== 'undefined' && WebGLRenderingContext.prototype) {
              const origGetParam = WebGLRenderingContext.prototype.getParameter;
              WebGLRenderingContext.prototype.getParameter = function(param) {
                if (param === 37445) return $webGlVendor; // UNMASKED_VENDOR_WEBGL
                if (param === 37446) return $webGlRenderer; // UNMASKED_RENDERER_WEBGL
                return origGetParam.apply(this, arguments);
              };
            }
            if (typeof WebGL2RenderingContext !== 'undefined' && WebGL2RenderingContext.prototype) {
              const origGetParam2 = WebGL2RenderingContext.prototype.getParameter;
              WebGL2RenderingContext.prototype.getParameter = function(param) {
                if (param === 37445) return $webGlVendor;
                if (param === 37446) return $webGlRenderer;
                return origGetParam2.apply(this, arguments);
              };
            }
          } catch(e) {}
          """ else ""}

          // 4. Safe Canvas Noise Injection
          ${if (profile.canvasNoiseEnabled) """
          try {
            if (typeof CanvasRenderingContext2D !== 'undefined' && CanvasRenderingContext2D.prototype.getImageData) {
              const origGetImageData = CanvasRenderingContext2D.prototype.getImageData;
              CanvasRenderingContext2D.prototype.getImageData = function() {
                const imgData = origGetImageData.apply(this, arguments);
                try {
                  if (imgData && imgData.data && imgData.data.length > 0) {
                    imgData.data[0] = (imgData.data[0] ^ 1) & 0xFF;
                  }
                } catch(err) {}
                return imgData;
              };
            }
          } catch(e) {}
          """ else ""}

          // 5. AudioContext Noise Injection
          ${if (profile.audioNoiseEnabled) """
          try {
            if (typeof AudioBuffer !== 'undefined' && AudioBuffer.prototype.getChannelData) {
              const origChannel = AudioBuffer.prototype.getChannelData;
              AudioBuffer.prototype.getChannelData = function() {
                const res = origChannel.apply(this, arguments);
                try {
                  if (res && res.length > 0) {
                    res[0] = res[0] + 0.0000001;
                  }
                } catch(err) {}
                return res;
              };
            }
          } catch(e) {}
          """ else ""}

          // 6. WebRTC IP Protection
          ${when (profile.webrtcPolicy) {
            "Disabled" -> """
              try {
                window.RTCPeerConnection = undefined;
                window.webkitRTCPeerConnection = undefined;
              } catch(e) {}
            """
            "Proxy IP Only", "Fake IP" -> """
              try {
                if (typeof window !== 'undefined' && window.RTCPeerConnection && window.RTCPeerConnection.prototype) {
                  const origOffer = window.RTCPeerConnection.prototype.createOffer;
                  if (origOffer) {
                    window.RTCPeerConnection.prototype.createOffer = function() {
                      return origOffer.apply(this, arguments).then(function(offer) {
                        if (offer && offer.sdp) {
                          offer.sdp = offer.sdp.replace(/(\r\nc=IN IP4 )(\d+\.\d+\.\d+\.\d+)/g, "$$1" + "198.51.100.1");
                        }
                        return offer;
                      });
                    };
                  }
                }
              } catch(e) {}
            """
            else -> ""
          }}

          // 7. Geolocation Spoofing
          ${if (profile.geolocationPolicy == "Custom" || profile.geolocationPolicy == "Based on IP") """
          try {
            if (typeof navigator !== 'undefined' && navigator.geolocation) {
              navigator.geolocation.getCurrentPosition = function(success, error, options) {
                if (typeof success === 'function') {
                  success({
                    coords: {
                      latitude: ${profile.latitude},
                      longitude: ${profile.longitude},
                      accuracy: ${profile.accuracyMeters},
                      altitude: null,
                      altitudeAccuracy: null,
                      heading: null,
                      speed: null
                    },
                    timestamp: Date.now()
                  });
                }
              };
              navigator.geolocation.watchPosition = function(success) {
                if (typeof success === 'function') {
                  success({
                    coords: {
                      latitude: ${profile.latitude},
                      longitude: ${profile.longitude},
                      accuracy: ${profile.accuracyMeters}
                    },
                    timestamp: Date.now()
                  });
                }
                return 1;
              };
            }
          } catch(e) {}
          """ else ""}

          // 8. Virtual Camera & Media Devices Emulation
          try {
            if (typeof navigator !== 'undefined') {
              if (!navigator.mediaDevices) {
                navigator.mediaDevices = {};
              }

              // Enumerate Devices with Virtual Camera
              navigator.mediaDevices.enumerateDevices = async function() {
                const devs = [
                  {
                    deviceId: 'phantom-vcam-01',
                    groupId: 'phantom-camera-group',
                    kind: 'videoinput',
                    label: $vCamNameSafe
                  },
                  {
                    deviceId: 'phantom-mic-01',
                    groupId: 'phantom-audio-group',
                    kind: 'audioinput',
                    label: 'Phantom Virtual HD Microphone'
                  },
                  {
                    deviceId: 'phantom-out-01',
                    groupId: 'phantom-audio-group',
                    kind: 'audiooutput',
                    label: 'Default Audio Output'
                  }
                ];
                return devs;
              };

              // Virtual Camera Stream Generator function
              function createVirtualCameraStream() {
                const width = 1280;
                const height = 720;
                const canvas = document.createElement('canvas');
                canvas.width = width;
                canvas.height = height;
                const ctx = canvas.getContext('2d');
                let frameCount = 0;

                const mode = $vCamModeSafe;
                const profileName = $profileNameSafe;
                const cameraLabel = $vCamNameSafe;
                const mediaType = $vCamMediaType;
                const mediaBase64 = $vCamMediaBase64;

                let customMediaElement = null;
                if ((mediaType === 'IMAGE' || mediaType === 'VIDEO') && mediaBase64 && mediaBase64.length > 20) {
                  try {
                    customMediaElement = new Image();
                    customMediaElement.crossOrigin = 'anonymous';
                    customMediaElement.src = mediaBase64;
                  } catch (e) {}
                }

                function drawFrame() {
                  frameCount++;
                  const now = new Date();
                  const timeStr = now.toTimeString().split(' ')[0] + '.' + String(now.getMilliseconds()).padStart(3, '0');

                  if (customMediaElement && customMediaElement.complete && customMediaElement.naturalWidth > 0) {
                    ctx.drawImage(customMediaElement, 0, 0, width, height);

                    // Authentic Live Stream Status Overlay
                    ctx.fillStyle = 'rgba(10, 16, 20, 0.65)';
                    ctx.fillRect(16, height - 42, 280, 28);
                    ctx.fillStyle = '#00E599';
                    ctx.font = 'bold 12px monospace';
                    ctx.textAlign = 'left';
                    ctx.fillText('LIVE STREAM ● ' + timeStr, 24, height - 23);

                    ctx.fillStyle = 'rgba(255, 255, 255, 0.85)';
                    ctx.font = '11px sans-serif';
                    ctx.textAlign = 'right';
                    ctx.fillText(cameraLabel, width - 20, height - 23);

                  } else if (mode === 'Matrix Digital Wave') {
                    // Cyber Matrix Green Wave
                    ctx.fillStyle = '#08110D';
                    ctx.fillRect(0, 0, width, height);

                    ctx.fillStyle = '#00A98A';
                    ctx.font = '24px monospace';
                    for (let x = 30; x < width; x += 60) {
                      const yOffset = (frameCount * 6 + x * 4) % (height + 100);
                      ctx.fillText(String.fromCharCode(0x30A0 + ((frameCount + x) % 96)), x, yOffset);
                    }

                    // Watermark Card
                    ctx.fillStyle = 'rgba(14, 28, 22, 0.85)';
                    ctx.fillRect(width / 2 - 260, height / 2 - 80, 520, 160);
                    ctx.strokeStyle = '#00A98A';
                    ctx.lineWidth = 2;
                    ctx.strokeRect(width / 2 - 260, height / 2 - 80, 520, 160);

                    ctx.fillStyle = '#00D1AB';
                    ctx.font = 'bold 28px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.fillText('PHANTOM VIRTUAL CAMERA', width / 2, height / 2 - 20);

                    ctx.fillStyle = '#E2E8F0';
                    ctx.font = '18px monospace';
                    ctx.fillText(cameraLabel + ' • ' + profileName, width / 2, height / 2 + 18);
                    ctx.fillText('LIVE • ' + timeStr, width / 2, height / 2 + 48);

                  } else if (mode === 'Cyber Avatar') {
                    // Modern Minimalist Silhouette & Scanner
                    ctx.fillStyle = '#0D141C';
                    ctx.fillRect(0, 0, width, height);

                    // Pulse scanner ring
                    const pulse = (Math.sin(frameCount * 0.08) + 1) * 20;
                    ctx.beginPath();
                    ctx.arc(width / 2, height / 2 - 30, 110 + pulse, 0, Math.PI * 2);
                    ctx.strokeStyle = 'rgba(0, 169, 138, 0.35)';
                    ctx.lineWidth = 4;
                    ctx.stroke();

                    // Avatar head & shoulders
                    ctx.fillStyle = '#1C2B38';
                    ctx.beginPath();
                    ctx.arc(width / 2, height / 2 - 50, 70, 0, Math.PI * 2);
                    ctx.fill();

                    ctx.beginPath();
                    ctx.arc(width / 2, height / 2 + 130, 130, Math.PI, Math.PI * 2);
                    ctx.fill();

                    // Shield emblem
                    ctx.fillStyle = '#00A98A';
                    ctx.font = 'bold 26px sans-serif';
                    ctx.textAlign = 'center';
                    ctx.fillText('STEALTH AVATAR FEED', width / 2, height / 2 + 200);
                    ctx.fillStyle = '#94A3B8';
                    ctx.font = '16px monospace';
                    ctx.fillText(profileName + ' • ' + timeStr, width / 2, height / 2 + 230);

                  } else {
                    // Default SMPTE Color Bars with Digital Clock
                    const bars = ['#C0C0C0', '#C0C000', '#00C0C0', '#00C000', '#C000C0', '#C00000', '#0000C0'];
                    const barWidth = width / bars.length;
                    for (let i = 0; i < bars.length; i++) {
                      ctx.fillStyle = bars[i];
                      ctx.fillRect(i * barWidth, 0, barWidth, height * 0.75);
                    }

                    // Bottom info section
                    ctx.fillStyle = '#111827';
                    ctx.fillRect(0, height * 0.75, width, height * 0.25);

                    // Live digital overlay
                    ctx.fillStyle = '#00A98A';
                    ctx.font = 'bold 30px monospace';
                    ctx.textAlign = 'center';
                    ctx.fillText('PHANTOM VIRTUAL CAMERA 1080p', width / 2, height * 0.83);

                    ctx.fillStyle = '#FFFFFF';
                    ctx.font = '22px monospace';
                    ctx.fillText(profileName + ' | ' + timeStr + ' | 30 FPS', width / 2, height * 0.92);
                  }
                }

                // Initial render
                drawFrame();
                // 30 FPS interval loop
                setInterval(drawFrame, 33);

                if (canvas.captureStream) {
                  const stream = canvas.captureStream(30);
                  const track = stream.getVideoTracks()[0];
                  if (track) {
                    safeDefine(track, 'label', () => $vCamNameSafe);
                  }
                  return stream;
                }
                return null;
              }

              ${if (vCamEnabled) """
              // Hook getUserMedia to return Virtual Camera
              navigator.mediaDevices.getUserMedia = async function(constraints) {
                if (constraints && constraints.video) {
                  const vStream = createVirtualCameraStream();
                  if (vStream) {
                    // If audio is also requested, add a silent or subtle audio track
                    if (constraints.audio && typeof AudioContext !== 'undefined') {
                      try {
                        const audioCtx = new AudioContext();
                        const dest = audioCtx.createMediaStreamDestination();
                        const osc = audioCtx.createOscillator();
                        const gain = audioCtx.createGain();
                        gain.gain.value = 0.0001; // virtually silent
                        osc.connect(gain);
                        gain.connect(dest);
                        osc.start();
                        const aTrack = dest.stream.getAudioTracks()[0];
                        if (aTrack) {
                          safeDefine(aTrack, 'label', () => 'Phantom Virtual HD Microphone');
                          vStream.addTrack(aTrack);
                        }
                      } catch(e) {}
                    }
                    return vStream;
                  }
                }
                throw new DOMException('Virtual Camera Feed unavailable', 'NotAllowedError');
              };

              // Legacy getUserMedia fallback
              if (navigator.getUserMedia) {
                navigator.getUserMedia = function(constraints, success, error) {
                  navigator.mediaDevices.getUserMedia(constraints).then(success).catch(error);
                };
              }
              if (navigator.webkitGetUserMedia) {
                navigator.webkitGetUserMedia = navigator.getUserMedia;
              }
              """ else ""}
            }
          } catch(e) {}

        } catch (err) {
          // Complete crash immunity
        }
      })();
    """.trimIndent()
  }
}
