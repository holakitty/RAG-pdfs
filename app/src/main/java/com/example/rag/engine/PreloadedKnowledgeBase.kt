package com.example.rag.engine

import com.example.rag.data.model.CuratedQuery
import com.example.rag.data.model.DocumentItem

object PreloadedKnowledgeBase {

    val documents: List<DocumentItem> = listOf(
        DocumentItem(
            id = "doc_quantum_computing",
            title = "Quantum Computing: Qubits, Superposition & Shor's Algorithm",
            category = "Quantum Physics & Computing",
            author = "Dr. Elena Vance, Quantum Information Labs",
            summary = "Foundational quantum computing principles, state superposition in Hilbert space, quantum entanglement, and quantum algorithmic complexity.",
            content = """
# Quantum Computing: Qubits, Superposition & Shor's Algorithm

Quantum computing harnesses quantum mechanical phenomena including state superposition, phase interference, and quantum entanglement to solve computational problems intractable on classical Turing architectures.

## 1. Principles of Qubits and Hilbert Space
A classical bit resides deterministically in state 0 or 1. A quantum bit (qubit) exists in a two-dimensional Hilbert space, mathematically expressed as |ψ⟩ = α|0⟩ + β|1⟩, where α and β are complex probability amplitudes satisfying the normalization constraint |α|² + |β|² = 1. The Bloch sphere visually maps this state onto spherical coordinates (θ, φ). When an n-qubit system is entangled, its state spans a 2^n dimensional vector space, enabling simultaneous manipulation of exponentially vast state configurations.

## 2. Quantum Logic Gates and Reversible Operations
Quantum logic operators are represented as unitary transformations U where U†U = I. Fundamental single-qubit gates include the Pauli matrices (X, Y, Z), Hadamard gate H which produces symmetric superposition H|0⟩ = (|0⟩+|1⟩)/√2, and phase rotation gates. The controlled-NOT (CNOT) and Toffoli gates establish non-local quantum correlations and universal quantum computation.

## 3. Shor's Algorithm for Prime Factorization
Peter Shor formulated a polynomial-time quantum algorithm in 1994 capable of factoring large composite integers N = p·q in O((log N)³) time complexity, fundamentally threatening classical RSA public-key cryptosystems. The core engine is Quantum Phase Estimation (QPE) over modular exponentiation functions f(x) = a^x mod N to discover the period r such that a^r ≡ 1 (mod N). Once the period r is extracted through the Quantum Fourier Transform (QFT), greatest common divisor calculations gcd(a^(r/2) ± 1, N) recover prime factors p and q with high probability.

## 4. Quantum Error Correction and Fault Tolerance
Current Noisy Intermediate-Scale Quantum (NISQ) devices suffer from environmental decoherence and gate infidelities. Surface codes and Steane [[7,1,3]] codes encode a single logical qubit across multiple physical qubits via topological parity checks, maintaining coherence when physical error rates fall below the fault-tolerance threshold (approximately 1%).
            """.trimIndent(),
            estimatedPages = 4
        ),
        DocumentItem(
            id = "doc_transformer_architecture",
            title = "Artificial Intelligence: Transformer Architecture & Multi-Head Attention",
            category = "Artificial Intelligence & ML",
            author = "Neural Computing Research Group",
            summary = "Detailed architectural analysis of the Transformer mechanism, scaled dot-product attention, positional encodings, and autoregressive decoding.",
            content = """
# Artificial Intelligence: Transformer Architecture & Multi-Head Attention

The Transformer architecture, introduced in 'Attention Is All You Need' (Vaswani et al.), eliminated recurrence and convolutions in sequence modeling, relying entirely on parallelized self-attention mechanisms.

## 1. Scaled Dot-Product Attention
Given packed matrix representations of queries Q, keys K, and values V with dimension d_k, the scaled dot-product attention computes:
Attention(Q, K, V) = softmax(Q·Kᵀ / √d_k) · V
The scaling factor 1/√d_k counteracts vanishing gradients caused by extremely large dot products pushing softmax functions into saturated regions with tiny derivatives.

## 2. Multi-Head Attention (MHA)
Rather than performing a single attention function with d_model-dimensional queries, Multi-Head Attention linearly projects queries, keys, and values h times with learned parameter matrices W_i^Q, W_i^K, and W_i^V to dimensions d_k, d_k, and d_v. MultiHead(Q,K,V) = Concat(head_1, ..., head_h)·W^O, where head_i = Attention(Q·W_i^Q, K·W_i^K, V·W_i^V). This empowers the network to attend simultaneously to information from distinct representation subspaces at differing positions.

## 3. Positional Encoding
Because self-attention operations are permutation-invariant, Transformers inject spatial order through positional encodings PE_(pos, 2i) = sin(pos / 10000^(2i/d_model)) and PE_(pos, 2i+1) = cos(pos / 10000^(2i/d_model)). Modern architectures frequently replace fixed sinusoidal encodings with Rotary Position Embeddings (RoPE), which encode relative positions via complex vector rotations.

## 4. Feed-Forward Networks & Residual Normalization
Each encoder layer couples multi-head attention with a position-wise Feed-Forward Network: FFN(x) = max(0, x·W_1 + b_1)·W_2 + b_2, or SwiGLU activations. Residual connections followed by Layer Normalization (or RMSNorm) stabilize gradient propagation across hundreds of transformer layers.
            """.trimIndent(),
            estimatedPages = 4
        ),
        DocumentItem(
            id = "doc_climate_carbon_budgets",
            title = "Climate Science: Carbon Budgets, Geoengineering & IPCC Pathways",
            category = "Environmental & Climate Science",
            author = "Intergovernmental Climate Dynamics Consortium",
            summary = "Atmospheric greenhouse gas radiative forcing, remaining global carbon budgets for 1.5°C stabilization, and solar radiation management risks.",
            content = """
# Climate Science: Carbon Budgets, Geoengineering & IPCC Pathways

Earth's climate system responds directly to anthropogenic radiative forcing driven by cumulative greenhouse gas emissions, altering radiative equilibrium and cryospheric stability.

## 1. Transient Climate Response to Cumulative Emissions (TCRE)
The Intergovernmental Panel on Climate Change (IPCC AR6) established a near-linear empirical relationship between cumulative CO₂ emissions and global surface air temperature anomalies: approximately 0.45°C per 1,000 GtCO₂. To retain a 50% probability of limiting warming to 1.5°C above pre-industrial levels, the remaining global carbon budget from 2024 onward is estimated under 250 GtCO₂, demanding net-zero trajectories before 2050.

## 2. Carbon Cycle Feedbacks and Tipping Elements
Positive feedback loops accelerate atmospheric warming: permafrost thawing releases methane (CH₄) and carbon dioxide, reduced Arctic albedo amplifies polar absorption, and Amazon forest dieback weakens biological carbon sinks. Key tipping cascades include the Atlantic Meridional Overturning Circulation (AMOC) slowdown and West Antarctic Ice Sheet destabilization.

## 3. Solar Radiation Management (SRM) and Stratospheric Aerosol Injection
Geoengineering proposals examine Stratospheric Aerosol Injection (SAI), dispersing sulfur dioxide (SO₂) particles into the stratosphere to mimic volcanic cooling (similar to Mt. Pinatubo 1991). While capable of lowering peak temperatures, SRM introduces grave systemic hazards: disruption of Asian and African monsoons, ocean acidification unmitigated by CO₂ levels, and lethal 'termination shock' if aerosol dispersal is abruptly halted.
            """.trimIndent(),
            estimatedPages = 3
        ),
        DocumentItem(
            id = "doc_neuroscience_bci",
            title = "Neuroscience: Neural Decoding & Brain-Computer Interfaces",
            category = "Neuroscience & Bioengineering",
            author = "Prof. Marcus Thorne, Center for Neural Engineering",
            summary = "Electrophysiological signal acquisition, microelectrode arrays, Kalman filter decoding of motor cortex activity, and invasive vs non-invasive BCIs.",
            content = """
# Neuroscience: Neural Decoding & Brain-Computer Interfaces

Brain-Computer Interfaces (BCIs) translate endogenous electrophysiological neural signals into computational commands for prosthetic limbs, speech synthesizers, and neuro-rehabilitation systems.

## 1. Electrophysiological Modalities
BCI systems employ three primary signal modalities:
- Non-Invasive Electroencephalography (EEG): Captures scalp potentials with high temporal resolution (~1 ms) but attenuated spatial resolution (>2 cm) due to skull volume conduction.
- Electrocorticography (ECoG): Subdural or epidural electrode grids resting directly on the cerebral cortex, balancing safety and signal clarity.
- Intracortical Microelectrode Arrays (Utah Array, Neuralink threads): Penetrate layer IV/V of the primary motor cortex (M1), capturing single-unit action potentials (spikes) and Local Field Potentials (LFPs) with cellular spatial fidelity.

## 2. Spike Sorting and Feature Extraction
Extracellular recordings pick up superpositioned electrical potentials from neighboring pyramidal neurons. Waveform thresholding followed by Principal Component Analysis (PCA) and Gaussian Mixture Clustering classifies voltage deflections into distinct neuronal units. Binned firing rates form high-dimensional kinematic state vectors.

## 3. Decoding Algorithms: From Kalman Filters to Deep Decoders
Motor kinematics (hand position, velocity, grip force) are reconstructed via recursive Bayesian estimation. Linear Kalman filters model neural firing as linear state observations. Modern closed-loop BCIs integrate Recurrent Neural Networks (RNNs) and Transformer decoders with real-time latency under 15 milliseconds, achieving over 90 words-per-minute attempted speech decoding directly from motor speech cortex.
            """.trimIndent(),
            estimatedPages = 4
        ),
        DocumentItem(
            id = "doc_space_artemis_isru",
            title = "Space Exploration: Artemis Program, Orbital Dynamics & Lunar ISRU",
            category = "Aerospace Engineering",
            author = "NASA Advanced Missions Directorate",
            summary = "Deep space architecture for human lunar return, Near-Rectilinear Halo Orbits (NRHO), cryogenic boil-off mitigation, and regolith volatile extraction.",
            content = """
# Space Exploration: Artemis Program, Orbital Dynamics & Lunar ISRU

Sustainable human presence on the Moon and Mars hinges on deep-space logistics, advanced orbital mechanics, and In-Situ Resource Utilization (ISRU) to reduce terrestrial supply chain dependence.

## 1. Lunar Gateway and Near-Rectilinear Halo Orbits (NRHO)
The Lunar Gateway station occupies a stable 9:7 resonant Earth-Moon L2 Near-Rectilinear Halo Orbit. NRHO offers perpetual line-of-sight communication with Earth, minimal station-keeping Δv requirements (~2-5 m/s per year), and unrestricted ballistic orbital access to the lunar South Pole, where permanently shadowed craters harbor volatile water ice deposits.

## 2. In-Situ Resource Utilization (ISRU) Architecture
Transporting 1 kg of propellant from Low Earth Orbit (LEO) to the lunar surface incurs substantial exponential mass ratios governed by Tsiolkovsky's Rocket Equation: Δv = I_sp · g_0 · ln(m_0 / m_f). Producing propellants in-situ on the Moon transforms mission economics:
- Polar Regolith Thermal Mining: Sublimating subsurface ice at temperatures above -50°C inside sealed domes, condensing H₂O vapor.
- Water Electrolysis & Liquefaction: Splitting H₂O into liquid oxygen (LOX) and liquid hydrogen (LH₂) to refuel human landers and Starship depot vehicles.
- Carbothermal Reduction: Heating lunar regolith with concentrated solar flux or methane to extract elemental oxygen bound within iron-titanium minerals (ilmenite FeTiO₃).

## 3. Cryogenic Fluid Management (CFM)
Deep space cryogenic storage of liquid hydrogen (20 K) and liquid oxygen (90 K) demands multi-layer insulation (MLI), sunshields, zero-boil-off active cryocoolers, and microgravity propellant transfer technologies.
            """.trimIndent(),
            estimatedPages = 4
        ),
        DocumentItem(
            id = "doc_renewable_perovskite_batteries",
            title = "Renewable Energy: Perovskite Tandem Cells & Grid-Scale Flow Batteries",
            category = "Clean Energy & Materials",
            author = "Renewable Energy Laboratory",
            summary = "Next-generation photovoltaic material science, Shockley-Queisser limit overstepping, metal-halide perovskites, and redox flow energy storage.",
            content = """
# Renewable Energy: Perovskite Tandem Cells & Grid-Scale Flow Batteries

Transitioning the global energy grid to intermittent wind and solar requires ultra-high-efficiency photovoltaic generation coupled with multi-megawatt long-duration energy storage.

## 1. Metal-Halide Perovskite Solar Cells
Conventional single-junction crystalline silicon PV tops out at a theoretical Shockley-Queisser efficiency limit of approximately 29.4%. Perovskites with crystal structure ABX₃ (e.g., methylammonium lead iodide CH₃NH₃PbI₃) boast tunable optical bandgaps (1.2 to 2.3 eV), high absorption coefficients, and long electron-hole diffusion lengths exceeding 1 micrometer.

## 2. Monolithic Perovskite-Silicon Tandem Architectures
By optically pairing a wide-bandgap (1.68 eV) top perovskite subcell with a narrow-bandgap (1.12 eV) bottom silicon heterojunction (SHJ) subcell, tandem devices absorb high-energy blue/green photons in the top layer while passing low-energy infrared photons to the bottom. Laboratory tandem conversion efficiencies have exceeded 33.9%, drastically lowering land and racking capital expenditures.

## 3. Vanadium Redox Flow Batteries (VRFB) for Grid Storage
Lithium-ion batteries present thermal runaway risks and degrade after 3,000-5,000 cycles, making them suboptimal for 12+ hour grid buffering. Vanadium Redox Flow Batteries decouple power (kW, dictated by stack membrane area) from energy capacity (kWh, dictated by external electrolyte tank volumes). Using vanadium in four distinct oxidation states (V²⁺/V³⁺ in negative half-cells and V⁴⁺/V⁵⁺ in positive half-cells), VRFBs offer over 20,000 cycles without electrolyte cross-contamination or degradation.
            """.trimIndent(),
            estimatedPages = 4
        ),
        DocumentItem(
            id = "doc_cybersecurity_zero_trust",
            title = "Cybersecurity: Zero-Trust Architectures & Post-Quantum Cryptography",
            category = "Cybersecurity & Cryptography",
            author = "National Cyber Defense Institute",
            summary = "Identity-centric zero-trust perimeter defense, micro-segmentation, and NIST standardized lattice-based post-quantum cryptography (Kyber, Dilithium).",
            content = """
# Cybersecurity: Zero-Trust Architectures & Post-Quantum Cryptography

Modern enterprise threat landscapes have dismantled the traditional castle-and-moat network perimeter. Zero-Trust Architecture (NIST SP 800-207) enforces a strict doctrine: 'Never Trust, Always Verify'.

## 1. Zero-Trust Core Tenants & Micro-Segmentation
Zero Trust mandates continuous authentication, ephemeral session-based authorization, and least-privilege access control. Every request—internal or external—is evaluated dynamically using context: device health, behavioral telemetry, geographic anomalies, and cryptographically verified mutual TLS (mTLS). Micro-segmentation decomposes corporate networks into granular security zones, restricting lateral movement by malicious actors.

## 2. Post-Quantum Cryptography (PQC) Transition
The imminent arrival of cryptanalytically relevant quantum computers (CRQCs) threatens to break all widely deployed asymmetric encryption (RSA, Diffie-Hellman, ECDSA) via Shor's algorithm. The 'Harvest Now, Decrypt Later' espionage campaign means adversaries are capturing encrypted transmissions today to decrypt post-quantum.

## 3. NIST Post-Quantum Standards: Lattice-Based Cryptography
In 2024, NIST published initial post-quantum standards centered on hard mathematical problems in Euclidean lattices:
- ML-KEM (Module-Lattice Key Encapsulation Mechanism, based on CRYSTALS-Kyber): Replaces ECDH key exchange. Derives security from the hardness of the Module Learning With Errors (M-LWE) problem.
- ML-DSA (Module-Lattice Digital Signature Algorithm, based on CRYSTALS-Dilithium): Provides post-quantum digital signatures for authentication and certificates.
- SLH-DSA (Stateless Hash-Based Digital Signature Algorithm, SPHINCS+): Acts as a mathematically conservative fallback immune to lattice vulnerabilities.
            """.trimIndent(),
            estimatedPages = 4
        ),
        DocumentItem(
            id = "doc_robotics_mpc_quadruped",
            title = "Robotics & Kinematics: Model Predictive Control & Quadruped Locomotion",
            category = "Robotics & Dynamic Systems",
            author = "Biomechatronics & Quadruped Dynamics Lab",
            summary = "Dynamic legged locomotion, single rigid body model simplifications, convex MPC optimization, and operational space controllers.",
            content = """
# Robotics & Kinematics: Model Predictive Control & Quadruped Locomotion

Dynamic legged robots like quadruped and bipedal platforms require high-frequency control loops to traverse rough, unstructured terrain while rejecting external mechanical perturbations.

## 1. Hierarchical Locomotion Control Architecture
Quadruped stabilization typically splits into a hierarchical structure:
1. High-Level Gait Planner: Determines foot-strike timings and contact sequences (trotting, bounding, galloping) using duty factor metrics.
2. Mid-Level Model Predictive Control (Convex MPC): Solves an optimal ground reaction force (GRF) trajectory over a finite future time horizon (~0.5 s, 10-30 Hz).
3. Low-Level Whole-Body / Operational Space Controller (WBC): Resolves joint torques via Quadratic Programming (QP) at 500-1000 Hz, enforcing friction cone and joint acceleration limits.

## 2. Single Rigid Body Dynamics (SRBD)
To compute MPC solutions in real-time, the robot's complex 12+ degree-of-freedom kinematics are approximated as a Single Rigid Body:
p̈ = (1/m) · ∑ f_i + g
L̇ = ∑ (r_i × f_i)
where p is center-of-mass position, L is angular momentum, f_i is the contact force applied by leg i, and r_i is the contact point relative to center of mass. Formulating SRBD with small angle assumptions yields a convex quadratic program solvable within milliseconds.

## 3. Sim-to-Real Reinforcement Learning
Contemporary robotics increasingly pairs classical MPC with Deep Reinforcement Learning (DRL) trained in massively parallel physics simulators (Isaac Gym). Domain randomization (varying friction coefficients, mass distribution, and actuator latencies) yields policies robust to physical perturbations and sensor noise.
            """.trimIndent(),
            estimatedPages = 4
        ),
        DocumentItem(
            id = "doc_crispr_synthetic_biology",
            title = "Synthetic Biology: CRISPR Prime Editing & Base Editing Mechanics",
            category = "Genomics & Molecular Biology",
            author = "Biomolecular Engineering Collaborative",
            summary = "Mechanisms of CRISPR-Cas9 genome editing, Cas9 nickase fusion proteins, base editors, and prime editing without double-strand breaks.",
            content = """
# Synthetic Biology: CRISPR Prime Editing & Base Editing Mechanics

Precision genome engineering has evolved from crude double-strand DNA breaks to search-and-replace base editing and prime editing technologies capable of correcting pathogenic point mutations without unwanted insertions/deletions.

## 1. Classical CRISPR-Cas9 and Non-Homologous End Joining (NHEJ)
The wild-type Streptococcus pyogenes Cas9 endonuclease couples with a synthetic single guide RNA (sgRNA) to recognize a 20-nucleotide target sequence adjacent to a 5'-NGG Protospacer Adjacent Motif (PAM). Cas9 induces a double-strand break (DSB) 3 base pairs upstream of the PAM. The host cell repairs the break via error-prone Non-Homologous End Joining (NHEJ), frequently introducing frameshift indels useful for gene knockouts, or Homology-Directed Repair (HDR), which suffers from low in vivo efficiency in post-mitotic tissues.

## 2. Cytosine and Adenine Base Editors (CBE & ABE)
Base editors merge a catalytically impaired Cas9 nickase (Cas9n D10A) with a deaminase enzyme:
- Cytosine Base Editors (CBE): Deaminate cytidine to uridine within a narrow editing window, subsequently repaired to thymidine (C·G to T·A transition).
- Adenine Base Editors (ABE): Engineered TadA deaminases convert adenine to inosine, interpreted by cellular polymerases as guanine (A·T to G·C transition).
Base editing circumvents double-strand breaks but is restricted to specific transition mutations and vulnerable to bystander edits.

## 3. Prime Editing: Search-and-Replace Genome Writing
Developed by David Liu's laboratory, Prime Editing fuses Cas9n to an engineered reverse transcriptase (M-MLV RT). The complex is programmed with a prime editing guide RNA (pegRNA) containing both the target-binding spacer and an extended 3' sequence encoding the desired genetic edit and Primer Binding Site (PBS). Cas9n nicks the non-target strand, the liberated flap hybridizes to the PBS, and reverse transcriptase synthesizes the edited DNA sequence directly into the genomic locus. Prime editing performs all 12 possible base transitions/transversions, precise insertions up to 40+ bp, and deletions without requiring DSBs.
            """.trimIndent(),
            estimatedPages = 4
        ),
        DocumentItem(
            id = "doc_astrophysics_gravitational_waves",
            title = "Astrophysics: Gravitational Waves, LIGO Interferometry & Black Holes",
            category = "Astrophysics & Relativistic Physics",
            author = "Gravitational Astrophysics Consortium",
            summary = "General relativistic spacetime perturbations, quadrupolar radiation formula, Michelson-Morley laser interferometry, and binary black hole inspirals.",
            content = """
# Astrophysics: Gravitational Waves, LIGO Interferometry & Black Holes

Albert Einstein's 1915 General Theory of Relativity predicted that accelerating asymmetric mass distributions radiate ripples in the fabric of spacetime traveling at the speed of light: gravitational waves (GWs).

## 1. The Quadrupole Formula and Gravitational Wave Strain
In the linearized weak-field metric g_μν = η_μν + h_μν, gravitational radiation is quadrupolar (l=2) with no monopole or dipole components due to conservation of mass-energy and linear momentum. The dimensionless strain tensor h measures fractional distortion in spatial displacement: h = 2·ΔL / L. For cosmic sources like merging stellar-mass binary black holes at cosmological distances, h is extraordinarily minuscule: approximately 10⁻²¹ on Earth.

## 2. Laser Interferometer Gravitational-Wave Observatory (LIGO)
LIGO detects strain h via dual L-shaped 4-kilometer arm Fabry-Pérot resonant cavities:
- Optical Path Amplification: Input laser power (1064 nm Nd:YAG) is recycled to over 750 kW of circulating power inside the arms, bouncing light hundreds of times to increase effective interaction length.
- Quantum Squeezed Light: Injected squeezed vacuum states mitigate photon shot noise at high frequencies and radiation pressure noise at low frequencies.
- Quadruple Pendulum Seismic Isolation: Active hydraulic actuators and quadruple pendulum glass-fiber suspensions isolate mirrors from terrestrial seismic vibrations by over 10 orders of magnitude above 10 Hz.

## 3. Waveform Morphology: Inspiral, Merger, and Ringdown
Gravitational wave transients like GW150914 exhibit three distinct relativistic phases:
1. Inspiral: Binary bodies orbit around common center of mass, losing energy and angular momentum to GW radiation. Frequency and amplitude chirp upward according to post-Newtonian expansions.
2. Merger: Event horizons touch; peak strain amplitude is achieved under extreme non-linear spacetime curvature.
3. Ringdown: The perturbed remnant black hole settles into a stationary Kerr black hole, radiating quasi-normal modes characterized strictly by mass M and spin parameter a (No-Hair Theorem).
            """.trimIndent(),
            estimatedPages = 4
        )
    )

    val curatedQueries: List<CuratedQuery> = listOf(
        CuratedQuery(
            id = "q_shor_factoring",
            query = "How does Shor's algorithm achieve polynomial-time factorization compared to classical algorithms?",
            category = "Quantum Physics & Computing",
            targetDocId = "doc_quantum_computing",
            targetDocTitle = "Quantum Computing: Qubits, Superposition & Shor's Algorithm",
            rationale = "Tests algorithmic complexity understanding, QPE, QFT, and RSA vulnerability.",
            readyAnswer = "Shor's algorithm factors large composite integers N = p·q in O((log N)³) polynomial time, bypassing classical sub-exponential General Number Field Sieve bounds. It reduces factoring to order-finding: discovering period r such that a^r ≡ 1 (mod N).\n\nBy applying Quantum Phase Estimation over modular exponentiation operators and evaluating the Quantum Fourier Transform across superposed qubit registers, period r is extracted in polynomial time. Classical greatest common divisors gcd(a^(r/2) ± 1, N) then directly yield prime factors p and q.",
            keyCitations = listOf("Quantum Computing: Qubits & Shor's Algorithm", "Section 3: Shor's Algorithm for Prime Factorization")
        ),
        CuratedQuery(
            id = "q_transformer_attention",
            query = "What is the mathematical formulation of scaled dot-product multi-head attention and why is 1/√d_k used?",
            category = "Artificial Intelligence & ML",
            targetDocId = "doc_transformer_architecture",
            targetDocTitle = "Artificial Intelligence: Transformer Architecture & Multi-Head Attention",
            rationale = "Evaluates architectural precision and mathematical reasoning for gradient stabilization.",
            readyAnswer = "Scaled dot-product attention is formulated as Attention(Q, K, V) = softmax((Q · Kᵀ) / √d_k) · V. The scaling factor 1/√d_k prevents vanishing gradients. When dimension d_k is large, dot products grow proportional to d_k, pushing the softmax exponential into flat saturation regions where derivatives approach zero.\n\nMulti-Head Attention projects queries, keys, and values into h distinct representation subspaces with learned parameter matrices. Each head computes independent attention patterns, and the concatenated results are projected back to the model dimension, enabling the network to jointly attend to information from different positions and representational subspaces.",
            keyCitations = listOf("Artificial Intelligence: Transformer Architecture", "Section 1: Scaled Dot-Product Attention")
        ),
        CuratedQuery(
            id = "q_climate_geoengineering",
            query = "What are the systemic hazards of Stratospheric Aerosol Injection (SAI) geoengineering and termination shock?",
            category = "Environmental & Climate Science",
            targetDocId = "doc_climate_carbon_budgets",
            targetDocTitle = "Climate Science: Carbon Budgets, Geoengineering & IPCC Pathways",
            rationale = "Tests comprehension of geoengineering risks, monsoon impacts, and rapid warming shocks.",
            readyAnswer = "Stratospheric Aerosol Injection disperses sulfur dioxide particles into the stratosphere to increase planetary albedo and reflect incoming solar radiation. While this can cool mean global temperatures, it introduces profound systemic hazards. Most critically, aerosol loading alters regional hydrologic cycles, perturbing the South Asian and African monsoon systems that billions of people depend on for food security and agriculture.\n\nFurthermore, solar radiation management does nothing to mitigate ongoing ocean acidification caused by dissolved carbon dioxide. The most catastrophic danger is termination shock. If an ongoing stratospheric injection program abruptly stops due to technical failure or conflict, the accumulated greenhouse forcing unmasks within a few years, causing temperatures to surge far more rapidly than ecosystems or human societies can adapt.",
            keyCitations = listOf("Climate Science: Carbon Budgets & IPCC Pathways", "Section 3: Solar Radiation Management (SRM)")
        ),
        CuratedQuery(
            id = "q_bci_decoding",
            query = "How do Kalman filters and neural networks decode motor intentions from cortical spike recordings?",
            category = "Neuroscience & Bioengineering",
            targetDocId = "doc_neuroscience_bci",
            targetDocTitle = "Neuroscience: Neural Decoding & Brain-Computer Interfaces",
            rationale = "Examines signal acquisition, PCA spike sorting, and state-space Bayesian estimation.",
            readyAnswer = "Intracortical microelectrode arrays in primary motor cortex capture extracellular action potentials from neuronal ensembles. After voltage thresholding and spike sorting, neural firing rates are decoded into continuous kinematic intentions through state-space Kalman filtering. The linear Gaussian state model estimates limb velocity by balancing expected trajectory physics against instantaneous neural firing observations.\n\nFor complex and non-linear behaviors, recurrent neural networks and transformer architectures decode motor trajectories over longer temporal horizons. These models adapt to daily electrode impedance changes and neural plasticity, maintaining decoding accuracy for prosthetic hands and computer cursors across multi-day sessions.",
            keyCitations = listOf("Neuroscience: Neural Decoding & BCIs", "Section 2: Spike Sorting & Kinematic Decoding")
        ),
        CuratedQuery(
            id = "q_artemis_isru",
            query = "How does In-Situ Resource Utilization (ISRU) overcome the rocket equation mass penalties for lunar missions?",
            category = "Aerospace Engineering",
            targetDocId = "doc_space_artemis_isru",
            targetDocTitle = "Space Exploration: Artemis Program, Orbital Dynamics & Lunar ISRU",
            rationale = "Focuses on lunar water ice extraction, thermal mining, and cryogenic propellant depots.",
            readyAnswer = "The classical rocket equation imposes severe mass penalties when all propellant for a return journey must be lifted from Earth's deep gravity well. Artemis in-situ resource utilization overcomes this barrier by extracting volatile water ice deposits located inside permanently shadowed lunar polar craters. Concentrated thermal systems sublimate the subsurface ice, which is purified and electrolyzed into liquid oxygen and liquid hydrogen.\n\nBecause propellant constitutes over eighty percent of the mass of a return spacecraft, refueling on the lunar surface eliminates the need to launch return fuel from Earth. This enables reusable lunar landers, establishes sustainable deep space logistics depots, and lowers the required Earth departure mass by more than seventy percent for crewed missions.",
            keyCitations = listOf("Space Exploration: Artemis & Lunar ISRU", "Section 2: Polar Volatiles & ISRU")
        ),
        CuratedQuery(
            id = "q_perovskite_tandem",
            query = "Why can perovskite-silicon tandem solar cells exceed the 29.4% Shockley-Queisser theoretical efficiency limit?",
            category = "Clean Energy & Materials",
            targetDocId = "doc_renewable_perovskite_batteries",
            targetDocTitle = "Renewable Energy: Perovskite Tandem Cells & Grid-Scale Flow Batteries",
            rationale = "Analyzes optical bandgap complementary splitting and tandem photovoltaic physics.",
            readyAnswer = "Single-junction silicon solar cells cannot surpass the theoretical 29.4 percent Shockley-Queisser limit because short-wavelength blue photons lose substantial energy as heat through thermalization, while long-wavelength infrared photons pass straight through without absorption. Monolithic perovskite-silicon tandem cells overcome this fundamental barrier by stacking two complementary semiconductors in series.\n\nThe top metal-halide perovskite cell has a wide bandgap tuned to absorb energetic ultraviolet and blue light with minimal thermal dissipation. The bottom silicon cell has a narrower bandgap that absorbs the transmitted red and near-infrared wavelengths. By dividing the solar spectrum between two tuned absorbers, tandem architectures reduce thermal losses and elevate achievable commercial efficiency beyond 33 to 35 percent.",
            keyCitations = listOf("Renewable Energy: Perovskite Tandems & Flow Batteries", "Section 1: Perovskite-Silicon Tandem Photovoltaics")
        ),
        CuratedQuery(
            id = "q_pqc_kyber",
            query = "How do lattice-based cryptographic algorithms like ML-KEM resist quantum attacks by Shor's algorithm?",
            category = "Cybersecurity & Cryptography",
            targetDocId = "doc_cybersecurity_zero_trust",
            targetDocTitle = "Cybersecurity: Zero-Trust Architectures & Post-Quantum Cryptography",
            rationale = "Validates understanding of Module-LWE hardness versus classical discrete log vulnerabilities.",
            readyAnswer = "Classical public key systems such as RSA and Elliptic Curve Diffie-Hellman depend on mathematical group structures whose periodicities can be solved in polynomial time using Shor's quantum algorithm. In contrast, modern lattice-based standards like ML-KEM rely on the hardness of the Module Learning With Errors problem over high-dimensional polynomial lattices.\n\nIn lattice-based cryptography, finding the closest vector in a high-dimensional lattice with intentional Gaussian noise exhibits no hidden periodicity that quantum Fourier transforms can exploit. Because no polynomial-time quantum algorithm is known to solve lattice vector problems, ML-KEM provides robust security against both classical supercomputers and future fault-tolerant quantum computers.",
            keyCitations = listOf("Cybersecurity: Zero-Trust & Post-Quantum Cryptography", "Section 3: Post-Quantum Cryptography (PQC) Standards")
        ),
        CuratedQuery(
            id = "q_robotics_srbd",
            query = "How does Single Rigid Body Dynamics (SRBD) simplify quadruped locomotion control in convex MPC?",
            category = "Robotics & Dynamic Systems",
            targetDocId = "doc_robotics_mpc_quadruped",
            targetDocTitle = "Robotics & Kinematics: Model Predictive Control & Quadruped Locomotion",
            rationale = "Tests formulation of center-of-mass angular momentum and ground reaction force optimization.",
            readyAnswer = "Full dynamic models for twelve degree-of-freedom quadruped robots require complex non-linear equations of motion that are computationally expensive to solve in real time. Single Rigid Body Dynamics simplifies this problem by treating the entire robot as a single lumped mass and inertia tensor centered at the center of mass, while assuming leg masses during swing are negligible.\n\nBy parameterizing scheduled foot contact points, the translational and rotational Newton-Euler equations become strictly linear with respect to ground reaction forces. This formulation converts optimal ground force selection into a convex quadratic program that can be solved at frequencies up to two hundred hertz, providing agile balance and rapid disturbance recovery over slippery or uneven terrain.",
            keyCitations = listOf("Robotics & Kinematics: MPC & Quadruped Locomotion", "Section 1: Single Rigid Body Dynamics (SRBD)")
        ),
        CuratedQuery(
            id = "q_crispr_prime_editing",
            query = "What makes Prime Editing with pegRNA fundamentally different from classical Cas9 double-strand break repair?",
            category = "Genomics & Molecular Biology",
            targetDocId = "doc_crispr_synthetic_biology",
            targetDocTitle = "Synthetic Biology: CRISPR Prime Editing & Base Editing Mechanics",
            rationale = "Evaluates reverse transcriptase mediated search-and-replace vs error-prone NHEJ indels.",
            readyAnswer = "Classical Cas9 creates blunt double-strand breaks in target DNA, which host cells typically repair using non-homologous end joining. This classical repair pathway is stochastic and frequently produces random insertions and deletions rather than precise edits. Prime editing fundamentally avoids double-strand breaks by employing an engineered fusion of a Cas9 nickase and an engineered reverse transcriptase guided by prime editing guide RNA.\n\nThe prime editing guide RNA specifies the genomic target, binds the single-strand DNA flap produced by the nickase, and directly templates reverse transcription of the desired sequence. This search-and-replace mechanism enables all twelve possible base-to-base transitions and transversions, as well as targeted micro-insertions and deletions, without inducing uncontrolled chromosome rearrangements.",
            keyCitations = listOf("Synthetic Biology: CRISPR Prime Editing Mechanics", "Section 2: Prime Editing Architecture")
        ),
        CuratedQuery(
            id = "q_ligo_gravitational_waves",
            query = "How does LIGO isolate seismic vibrations and use squeezed vacuum light to measure 10^-21 strain?",
            category = "Astrophysics & Relativistic Physics",
            targetDocId = "doc_astrophysics_gravitational_waves",
            targetDocTitle = "Astrophysics: Gravitational Waves, LIGO Interferometry & Black Holes",
            rationale = "Explains quadruple pendulum isolation, Fabry-Pérot resonant arm cavities, and quantum noise.",
            readyAnswer = "Measuring gravitational wave strains on the order of 10^-21 requires isolating the detector optics from terrestrial vibrations and mitigating quantum noise limits. LIGO suspends its heavy fused-silica mirrors using quadruple pendulum systems with monolithic glass fibers, combined with active hydraulic platforms that attenuate ground motion by over ten orders of magnitude at frequencies above ten hertz.\n\nTo beat the quantum shot noise caused by photon arrival statistics, LIGO injects phase-squeezed vacuum light into the interferometer's dark port. Squeezing redistributes quantum uncertainty away from the phase quadrature, reducing high-frequency measurement noise without requiring higher laser powers that would thermally distort the optics.",
            keyCitations = listOf("Astrophysics: Gravitational Waves & LIGO", "Section 2: Seismic Isolation & Squeezed Light")
        ),
        CuratedQuery(
            id = "q_cross_quantum_security",
            query = "Synthesize how advances in quantum algorithms (Doc 1) impact modern zero-trust enterprise security (Doc 7).",
            category = "Cross-Domain Synthesis",
            targetDocId = "doc_quantum_computing",
            targetDocTitle = "Quantum Computing & Cybersecurity",
            rationale = "Cross-document synthesis: evaluates multi-document retrieval and holistic reasoning.",
            readyAnswer = "Advances in fault-tolerant quantum computing running Shor's algorithm will compromise the asymmetric cryptography currently safeguarding modern zero-trust enterprise security. Zero-trust networks rely on continuous authentication, mutual TLS encrypted tunnels, and micro-segmentation, all of which depend on RSA or elliptic curve signatures. If adversaries record encrypted enterprise traffic today, they can decrypt it retrospectively once a cryptanalytically relevant quantum computer becomes operational.\n\nTo defend zero-trust architectures against harvest-now-decrypt-later attacks, enterprises must transition identity tokens and cryptographic session handshakes to quantum-resistant lattice algorithms like ML-KEM and ML-DSA. Implementing cryptographic agility across software-defined perimeters ensures continuous mutual authentication without relying on vulnerable classical key exchanges.",
            keyCitations = listOf("Doc 1: Quantum Computing (Shor's Algorithm)", "Doc 7: Cybersecurity (Zero-Trust & PQC Standards)")
        ),
        CuratedQuery(
            id = "q_cross_lunar_energy",
            query = "How could autonomous quadruped rovers (Doc 8) support lunar ISRU power infrastructure (Docs 5 & 6)?",
            category = "Cross-Domain Synthesis",
            targetDocId = "doc_space_artemis_isru",
            targetDocTitle = "Space, Energy & Robotics",
            rationale = "Multi-document query spanning space exploration, solar energy, and dynamic robotics.",
            readyAnswer = "Autonomous quadruped rovers provide crucial mobile support for lunar power generation infrastructure. Operating in the harsh terrain of the lunar South Pole, wheeled rovers struggle with steep boulder-strewn crater rims. Quadruped robots governed by convex model predictive control can navigate these loose regolith slopes to inspect and deploy high-efficiency perovskite-silicon tandem solar arrays along sunlit crater ridges.\n\nIn addition to inspecting solar panels and clearing abrasive lunar dust, quadrupeds can pull power distribution tethers from ridge-top solar arrays down into permanently shadowed crater floors. By delivering reliable electricity to cryogenic water ice extraction and electrolysis facilities in dark craters, autonomous legged robots enable continuous fuel production for the Artemis mission architecture.",
            keyCitations = listOf("Doc 5: Artemis Program & Lunar ISRU", "Doc 6: Perovskite Tandem Solar Cells", "Doc 8: Quadruped Locomotion MPC")
        )
    )
}
