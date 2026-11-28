"""Compose draft storefront graphics from the pinned existing Crossroads home art.
No runtime files modified. No fabricated screenshots. Owner review required.
"""
from pathlib import Path
import hashlib
import json
from PIL import Image, ImageDraw, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "app/src/main/assets/art/v1/home_hero.png"
OUT = ROOT / "marketing-output"
EXPECTED = "b338bf65526e9b3a6fccaff571edd0f9e66a5e9f21c20af91f19b8dd1c5b42e8"
FOREST = "#203d32"
PARCHMENT = "#f2e6c7"
GOLD = "#c6a66a"
SERIF = "/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf"
SANS = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
assert digest(ART) == EXPECTED, "Home hero fingerprint mismatch"
art = Image.open(ART).convert("RGB")
assert art.size == (1600, 640)
OUT.mkdir(exist_ok=True)

def fit_font(draw, text, path, size, max_width):
    while size > 12:
        font = ImageFont.truetype(path, size)
        if draw.textlength(text, font=font) <= max_width:
            return font
        size -= 1
    raise ValueError("Text too wide")

def line(draw, text, x, y, width, size, path=SERIF, fill=PARCHMENT):
    font = fit_font(draw, text, path, size, width)
    draw.text((x, y), text, font=font, fill=fill, anchor="mm")

def cover():
    w,h = 1260,1000
    im = Image.new("RGB",(w,h),FOREST)
    im.paste(ImageOps.fit(art,(w-32,558),centering=(.5,.5)),(16,16))
    d=ImageDraw.Draw(im)
    d.rectangle((16,16,w-17,h-17),outline=GOLD,width=5)
    d.line((16,574,w-17,574),fill=GOLD,width=5)
    line(d,"GRIDMATES",w//2,632,1100,49,SANS,GOLD)
    line(d,"CROSSROADS",w//2,738,1140,119)
    line(d,"Every path is a choice.",w//2,830,1100,48,SANS)
    line(d,"A woodland board game",w//2,916,1100,37,SANS,GOLD)
    return im

def wide(w,h):
    im=Image.new("RGB",(w,h),FOREST)
    left=round(w*.50)
    im.paste(ImageOps.fit(art,(w-left-12,h-24),centering=(.5,.5)),(left,12))
    d=ImageDraw.Draw(im)
    d.rectangle((12,12,w-13,h-13),outline=GOLD,width=3)
    d.line((left,12,left,h-13),fill=GOLD,width=3)
    x=left//2
    line(d,"GRIDMATES",x,round(h*.22),left-60,round(h*.065),SANS,GOLD)
    line(d,"CROSSROADS",x,round(h*.45),left-60,round(h*.17))
    line(d,"Every path is a choice.",x,round(h*.67),left-60,round(h*.075),SANS)
    line(d,"Solo • Local Pass & Play",x,round(h*.83),left-60,round(h*.055),SANS,GOLD)
    return im

TOKEN = ART.parent / "token_atlas_v1.png"
TOKEN_SHA = "ca2f8dceb3679dff75e24870f4a5b993458b3f853b69df48b6c28a43bed570dc"
assert digest(TOKEN) == TOKEN_SHA, "Token atlas fingerprint mismatch"
tokens = Image.open(TOKEN).convert("RGBA")
assert tokens.size == (512,128)

def board_cover():
    # Promotional composition, not a screenshot or an actual course diagram.
    im = cover()
    d = ImageDraw.Draw(im)
    # A small tabletop-inspired medallion strip over the scenic panel.
    d.rounded_rectangle((244,397,1016,566),radius=38,fill=FOREST,outline=GOLD,width=5)
    d.line([(326,482),(934,482)],fill=GOLD,width=8)
    for i,x in enumerate((326,529,731,934)):
        d.ellipse((x-69,413,x+69,551),fill=PARCHMENT,outline=GOLD,width=5)
        piece=tokens.crop((i*128,0,(i+1)*128,128))
        piece.thumbnail((116,116),Image.Resampling.LANCZOS)
        im.paste(piece,(x-piece.width//2,482-piece.height//2),piece)
    return im

board_master=board_cover()
master=cover()
files={
    "cover-master.png":master,
    "itch-cover.png":master.resize((630,500),Image.Resampling.LANCZOS),
    "thumbnail-review.png":master.resize((315,250),Image.Resampling.LANCZOS),
    "board-cover-master.png":board_master,
    "board-itch-cover.png":board_master.resize((630,500),Image.Resampling.LANCZOS),
    "board-thumbnail-review.png":board_master.resize((315,250),Image.Resampling.LANCZOS),
    "page-banner.png":wide(1600,480),
    "social-card.png":wide(1200,630),
}
receipt={"status":"draft-owner-review-required","source":str(ART.relative_to(ROOT)),"source_sha256":EXPECTED,"source_review":"qa-preview-only-not-owner-approved","assets":[]}
receipt["additional_sources"]=[{"path":str(TOKEN.relative_to(ROOT)),"sha256":TOKEN_SHA,"review":"qa-preview-only-not-owner-approved"}]
for name,img in files.items():
    path=OUT/name
    img.save(path,optimize=True)
    receipt["assets"].append({"name":name,"width":img.width,"height":img.height,"sha256":digest(path),"bytes":path.stat().st_size})
(OUT/"receipt.json").write_text(json.dumps(receipt,indent=2)+"\n")
print(json.dumps(receipt,indent=2))
