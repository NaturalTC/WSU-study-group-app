import { useId } from 'react'

function toRoman(n) {
  if (n >= 5000) return 'V̄'
  const vals = [1000,900,500,400,100,90,50,40,10,9,5,4,1]
  const syms = ['M','CM','D','CD','C','XC','L','XL','X','IX','V','IV','I']
  let r = ''
  for (let i = 0; i < vals.length; i++) {
    while (n >= vals[i]) { r += syms[i]; n -= vals[i] }
  }
  return r
}

// Shape = category identity. Each category always renders the same shape.
const CATEGORY_SHAPE = {
  logins:   'shield',
  streak:   'flame',
  messages: 'bubble',
  emojis:   'circle',
  groups:   'hexagon',
  sessions: 'book',
  points:   'trophy',
  helper:   'heart',
}

// Outer path = visible frame/border. Inner path = recessed face.
// All coordinates within a 40×40 viewBox.
const SHAPES = {
  // logins — classic downward shield
  shield: {
    outer: 'M3,3 L37,3 L37,22 L20,37 L3,22 Z',
    inner: 'M7,7 L33,7 L33,21 L20,32 L7,21 Z',
    textY: 19,
  },
  // streak — teardrop flame (narrow tip at top, full at bottom)
  flame: {
    outer: 'M20,2 C14,4 4,12 6,22 C8,32 14,38 20,38 C26,38 32,32 34,22 C36,12 26,4 20,2 Z',
    inner: 'M20,7 C15,9 8,16 10,22 C12,30 16,34 20,34 C24,34 28,30 30,22 C32,16 25,9 20,7 Z',
    textY: 23,
  },
  // messages — speech bubble with tail at bottom-center
  bubble: {
    outer: 'M8,2 Q2,2 2,8 L2,24 Q2,30 8,30 L15,30 L20,38 L25,30 L32,30 Q38,30 38,24 L38,8 Q38,2 32,2 Z',
    inner: 'M10,6 Q6,6 6,10 L6,22 Q6,26 10,26 L16,26 L20,32 L24,26 L30,26 Q34,26 34,22 L34,10 Q34,6 30,6 Z',
    textY: 16,
  },
  // emojis — circle (represents a face)
  circle: {
    isCircle: true,
    outerR: 18,
    innerR: 13,
    textY: 21,
  },
  // groups — hexagon (network/hive)
  hexagon: {
    outer: 'M10,2 L30,2 L38,20 L30,38 L10,38 L2,20 Z',
    inner: 'M13,7 L27,7 L33,20 L27,33 L13,33 L7,20 Z',
    textY: 21,
  },
  // sessions — open book (two pages meeting at a spine)
  book: {
    outer: 'M2,8 Q2,4 6,4 L19,6 L20,8 L21,6 L34,4 Q38,4 38,8 L38,34 Q38,36 34,36 L21,34 L20,36 L19,34 L6,36 Q2,36 2,34 Z',
    inner: 'M5,11 Q5,8 8,8 L19,10 L20,12 L21,10 L32,8 Q35,8 35,11 L35,31 Q35,34 32,34 L21,32 L20,34 L19,32 L8,34 Q5,34 5,31 Z',
    textY: 22,
  },
  // points — trophy cup with base (two subpaths)
  trophy: {
    outer: 'M8,2 L32,2 L32,20 Q32,30 20,32 Q8,30 8,20 Z M14,32 L26,32 L28,38 L12,38 Z',
    inner: 'M11,5 L29,5 L29,19 Q29,27 20,29 Q11,27 11,19 Z M15,29 L25,29 L27,35 L13,35 Z',
    textY: 15,
  },
  // helper — heart
  heart: {
    outer: 'M20,34 C10,27 2,20 2,12 Q2,2 12,4 Q16,4 20,10 Q24,4 28,4 Q38,2 38,12 C38,20 30,27 20,34 Z',
    inner: 'M20,29 C12,23 6,18 6,12 Q6,7 13,8 Q16,8 20,13 Q24,8 27,8 Q34,7 34,12 C34,18 28,23 20,29 Z',
    textY: 19,
  },
}

// Badge tier colors — independent achievement progression, not tied to leaderboard ranks.
// 1=Bronze · 2=Silver · 3=Gold · 4=Emerald · 5=Amethyst
// Badge tier colors — independent achievement progression, not tied to leaderboard ranks.
// 1=Bronze · 2=Silver · 3=Gold · 4=Emerald · 5=Amethyst
const TIER_COLORS = {
  1: { frameLight: '#f5a44c', frameMid: '#d4641a', frameDark: '#8b3a08', faceCenter: '#fff5e8', faceEdge: '#ffd4a0', numColor: '#7a2e06' },
  2: { frameLight: '#d0e4f8', frameMid: '#7898c0', frameDark: '#3a5878', faceCenter: '#f0f6ff', faceEdge: '#b8d0ec', numColor: '#2e4868' },
  3: { frameLight: '#ffe040', frameMid: '#f0aa00', frameDark: '#a07000', faceCenter: '#fffce0', faceEdge: '#ffe880', numColor: '#785000' },
  4: { frameLight: '#3aeaa0', frameMid: '#00b860', frameDark: '#006838', faceCenter: '#e8ffee', faceEdge: '#80ffb8', numColor: '#005030' },
  5: { frameLight: '#cc66ff', frameMid: '#9900ee', frameDark: '#5a0090', faceCenter: '#f8e8ff', faceEdge: '#e0a0ff', numColor: '#6600a0' },
}

const SIZE_CLASSES = { sm: 'w-8 h-8', md: 'w-10 h-10', lg: 'w-14 h-14' }

export default function BadgeIcon({ badge, size = 'md', showTooltip = true }) {
  const uid  = useId().replace(/:/g, '')
  if (!badge) return null

  const colors    = TIER_COLORS[badge.tier] ?? TIER_COLORS[1]
  const shapeName = CATEGORY_SHAPE[badge.category] ?? 'shield'
  const shape     = SHAPES[shapeName]
  const roman     = toRoman(badge.tier ?? 1)
  const visLen    = roman.replace(/̄/g, '').length
  const fontSize  = visLen <= 1 ? 16 : visLen <= 2 ? 13 : visLen <= 3 ? 10 : 8

  const frameGradId  = `fg${uid}`
  const highlightId  = `hi${uid}`
  const faceGradId   = `rg${uid}`
  const shadowId     = `sh${uid}`

  return (
    <div className={`relative group inline-flex flex-shrink-0 overflow-visible ${SIZE_CLASSES[size] ?? SIZE_CLASSES.md}`}>
      <svg viewBox="0 0 40 40" className="w-full h-full overflow-visible">
        <defs>
          {/* Metallic frame gradient — diagonal, lighter top-left to darker bottom-right */}
          <linearGradient id={frameGradId} x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%"   stopColor={colors.frameLight} />
            <stop offset="45%"  stopColor={colors.frameMid}   />
            <stop offset="100%" stopColor={colors.frameDark}  />
          </linearGradient>

          {/* Highlight shimmer — white top-left, dark bottom-right */}
          <linearGradient id={highlightId} x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%"   stopColor="white" stopOpacity="0.45" />
            <stop offset="45%"  stopColor="white" stopOpacity="0"    />
            <stop offset="100%" stopColor="black" stopOpacity="0.25" />
          </linearGradient>

          {/* Face gradient — bright white center fading to a light tier-tinted edge */}
          <radialGradient id={faceGradId} cx="38%" cy="32%" r="75%">
            <stop offset="0%"   stopColor={colors.faceCenter} stopOpacity="1"   />
            <stop offset="70%"  stopColor={colors.faceCenter} stopOpacity="0.95" />
            <stop offset="100%" stopColor={colors.faceEdge}   stopOpacity="1"   />
          </radialGradient>

          {/* Drop shadow filter */}
          <filter id={shadowId} x="-30%" y="-30%" width="160%" height="160%">
            <feDropShadow dx="1" dy="2" stdDeviation="2.5" floodColor="rgba(0,0,0,0.6)" />
          </filter>
        </defs>

        <g filter={`url(#${shadowId})`}>
          {shape.isCircle ? (
            <>
              <circle cx="20" cy="20" r={shape.outerR} fill={`url(#${frameGradId})`} />
              <circle cx="20" cy="20" r={shape.outerR} fill={`url(#${highlightId})`} />
              <circle cx="20" cy="20" r={shape.innerR} fill={`url(#${faceGradId})`} />
            </>
          ) : (
            <>
              <path d={shape.outer} fill={`url(#${frameGradId})`} />
              <path d={shape.outer} fill={`url(#${highlightId})`} />
              <path d={shape.inner} fill={`url(#${faceGradId})`} />
            </>
          )}

          {/* Roman numeral — black fill, colored stroke outline */}
          <text
            x="20"
            y={shape.textY}
            textAnchor="middle"
            dominantBaseline="central"
            fontSize={fontSize}
            fontWeight="bold"
            fontFamily="Georgia, 'Times New Roman', serif"
            fill={colors.numColor}
            stroke="white"
            strokeWidth="0.5"
            paintOrder="stroke fill"
          >
            {roman}
          </text>
        </g>
      </svg>

      {showTooltip && (
        <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 z-50 pointer-events-none
                        hidden group-hover:block w-max max-w-[160px]
                        bg-gray-900 dark:bg-gray-800 text-white text-xs rounded-xl
                        px-3 py-2 text-center shadow-xl">
          <p className="font-semibold leading-snug">{badge.name}</p>
          <p className="text-gray-300 text-[10px] mt-0.5 leading-snug">{badge.description}</p>
          {badge.earnedAt && (
            <p className="text-gray-400 text-[10px] mt-1">
              {new Date(badge.earnedAt).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' })}
            </p>
          )}
        </div>
      )}
    </div>
  )
}
