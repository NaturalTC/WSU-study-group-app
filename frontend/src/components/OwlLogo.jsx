function OwlLogo({ size = 32, className = '' }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 40 40"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      aria-label="WSU Owl"
    >
      {/* Ear tufts */}
      <path d="M12 13 L9 4 L16 11" fill="white" />
      <path d="M28 13 L31 4 L24 11" fill="white" />

      {/* Body / head */}
      <ellipse cx="20" cy="24" rx="12" ry="13" fill="white" />

      {/* Eye outer rings */}
      <circle cx="14.5" cy="21" r="5" fill="#F2A900" />
      <circle cx="25.5" cy="21" r="5" fill="#F2A900" />

      {/* Eye whites */}
      <circle cx="14.5" cy="21" r="3.5" fill="white" />
      <circle cx="25.5" cy="21" r="3.5" fill="white" />

      {/* Pupils */}
      <circle cx="14.5" cy="21" r="2" fill="#1B2A4A" />
      <circle cx="25.5" cy="21" r="2" fill="#1B2A4A" />

      {/* Eye shine */}
      <circle cx="15.3" cy="20.2" r="0.7" fill="white" />
      <circle cx="26.3" cy="20.2" r="0.7" fill="white" />

      {/* Beak */}
      <polygon points="18,25 20,28.5 22,25" fill="#F2A900" />

      {/* Belly lines */}
      <path d="M15 30 Q20 33 25 30" stroke="#E5E7EB" strokeWidth="1.2" fill="none" strokeLinecap="round" />
      <path d="M16 33 Q20 35.5 24 33" stroke="#E5E7EB" strokeWidth="1.2" fill="none" strokeLinecap="round" />
    </svg>
  )
}

export default OwlLogo
