import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Shield,
    ShieldAlert,
    Bell,
    Brain,
    Users,
    BarChart3,
    CheckCircle,
    ArrowRight,
    Lock,
    Eye,
    Zap,
    Globe,
    BookOpen,
    ChevronDown,
    Menu,
    X,
    GraduationCap,
    Settings,
} from 'lucide-react';

// ==========================================
// NAVBAR
// ==========================================
const LandingNavbar = ({ onLogin, onRegister }) => {

    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isScrolled, setIsScrolled] = useState(false);

    useEffect(() => {
        const handleScroll = () => {
            setIsScrolled(window.scrollY > 20);
        };
        window.addEventListener('scroll', handleScroll);
        return () =>
            window.removeEventListener(
                'scroll', handleScroll
            );
    }, []);

    return (
        <nav className={`fixed top-0 left-0 right-0 z-50
                         transition-all duration-300
                         ${isScrolled
                             ? 'bg-dark-500/95 backdrop-blur-xl border-b border-white/5'
                             : 'bg-transparent'
                         }`}>
            <div className="max-w-7xl mx-auto px-6">
                <div className="flex items-center
                                justify-between h-16">

                    {/* Logo */}
                    <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-xl
                                        bg-cyan-500/20
                                        border border-cyan-500/30
                                        flex items-center
                                        justify-center">
                            <Shield className="w-5 h-5
                                               text-cyan-400"/>
                        </div>
                        <span className="text-lg font-bold
                                         text-white">
                            CyberShield
                        </span>
                    </div>

                    {/* Desktop Nav Links */}
                    <div className="hidden md:flex
                                    items-center gap-8">
                        {[
                            'Features',
                            'How It Works',
                            'About',
                        ].map(item => (
                            <a key={item}
                               href={`#${item.toLowerCase()
                                   .replace(' ', '-')}`}
                               className="text-sm
                                          text-slate-400
                                          hover:text-cyan-400
                                          transition-colors">
                                {item}
                            </a>
                        ))}
                    </div>

                    {/* Auth Buttons */}
                    <div className="hidden md:flex
                                    items-center gap-3">
                        <button
                            onClick={onLogin}
                            className="px-4 py-2 text-sm
                                       text-slate-300
                                       hover:text-white
                                       transition-colors">
                            Sign In
                        </button>
                        <button
                            onClick={onRegister}
                            className="btn-cyber px-5
                                       py-2 text-sm">
                            Get Started
                        </button>
                    </div>

                    {/* Mobile Menu Toggle */}
                    <button
                        onClick={() =>
                            setIsMenuOpen(!isMenuOpen)
                        }
                        className="md:hidden text-slate-400
                                   hover:text-white">
                        {isMenuOpen
                            ? <X    className="w-5 h-5"/>
                            : <Menu className="w-5 h-5"/>
                        }
                    </button>
                </div>

                {/* Mobile Dropdown */}
                {isMenuOpen && (
                    <div className="md:hidden py-4
                                    border-t border-white/5
                                    space-y-3">
                        {[
                            'Features',
                            'How It Works',
                            'About',
                        ].map(item => (
                            <a key={item}
                               href={`#${item.toLowerCase()
                                   .replace(' ', '-')}`}
                               className="block text-sm
                                          text-slate-400
                                          hover:text-cyan-400
                                          py-2
                                          transition-colors">
                                {item}
                            </a>
                        ))}
                        <div className="flex gap-3 pt-2">
                            <button
                                onClick={onLogin}
                                className="flex-1 py-2
                                           rounded-xl
                                           border
                                           border-white/10
                                           text-sm
                                           text-slate-300">
                                Sign In
                            </button>
                            <button
                                onClick={onRegister}
                                className="flex-1 btn-cyber
                                           py-2 text-sm">
                                Get Started
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </nav>
    );
};

// ==========================================
// HERO SECTION
// ==========================================
const HeroSection = ({ onRegister, onLogin }) => (
    <section className="min-h-screen flex items-center
                        justify-center relative
                        overflow-hidden pt-16">

        {/* Background Effects */}
        <div className="absolute inset-0">
            <div className="absolute top-1/4 left-1/4
                            w-96 h-96 rounded-full
                            bg-cyan-500/8 blur-3xl"/>
            <div className="absolute bottom-1/4 right-1/4
                            w-96 h-96 rounded-full
                            bg-blue-500/8 blur-3xl"/>
            <div className="absolute top-1/2 left-1/2
                            -translate-x-1/2
                            -translate-y-1/2
                            w-[600px] h-[600px]
                            rounded-full
                            bg-purple-500/4 blur-3xl"/>
        </div>

        {/* Grid Pattern */}
        <div className="absolute inset-0 opacity-5"
             style={{
                 backgroundImage: `
                     linear-gradient(
                     rgba(0,212,255,0.3) 1px,
                     transparent 1px),
                     linear-gradient(90deg,
                     rgba(0,212,255,0.3) 1px,
                     transparent 1px)`,
                 backgroundSize: '60px 60px',
             }}/>

        <div className="relative text-center px-6
                        max-w-5xl mx-auto">

            {/* Badge */}
            <div className="inline-flex items-center gap-2
                            px-4 py-2 rounded-full
                            bg-cyan-500/10
                            border border-cyan-500/20
                            mb-8 animate-fade-in">
                <Zap className="w-3 h-3 text-cyan-400"/>
                <span className="text-xs text-cyan-400
                                 font-medium">
                    AI-Powered Cybersecurity Platform
                </span>
            </div>

            {/* Main Heading */}
            <h1 className="text-5xl md:text-7xl
                           font-bold text-white
                           leading-tight mb-6
                           animate-fade-in">
                Protect Your
                <span className="block text-gradient-blue">
                    Institution
                </span>
                From Cyber Threats
            </h1>

            {/* Subtitle */}
            <p className="text-lg text-slate-400
                          max-w-2xl mx-auto mb-10
                          leading-relaxed animate-fade-in">
                An intelligent threat monitoring system
                that empowers educational institutions
                to detect, report, and respond to
                cybersecurity incidents in real-time.
            </p>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row
                            items-center justify-center
                            gap-4 mb-16 animate-fade-in">
                <button
                    onClick={onRegister}
                    className="btn-cyber flex items-center
                               gap-2 px-8 py-4 text-base
                               font-semibold
                               w-full sm:w-auto">
                    <GraduationCap className="w-5 h-5"/>
                    Student Register Free
                    <ArrowRight className="w-4 h-4"/>
                </button>
                <button
                    onClick={onLogin}
                    className="flex items-center gap-2
                               px-8 py-4 rounded-xl
                               border border-white/10
                               text-slate-300
                               hover:border-cyan-500/30
                               hover:text-cyan-400
                               transition-all text-base
                               w-full sm:w-auto">
                    <Eye className="w-5 h-5"/>
                    Sign In to Dashboard
                </button>
            </div>

            {/* Stats Row */}
            <div className="grid grid-cols-3 gap-6
                            max-w-lg mx-auto">
                {[
                    { value: '99.9%', label: 'Uptime'          },
                    { value: '<2min', label: 'Detection Time'   },
                    { value: '100%',  label: 'Free to Use'      },
                ].map((stat, i) => (
                    <div key={i} className="text-center">
                        <p className="text-2xl font-bold
                                      text-gradient-blue">
                            {stat.value}
                        </p>
                        <p className="text-xs text-slate-500
                                      mt-1">
                            {stat.label}
                        </p>
                    </div>
                ))}
            </div>

            {/* Scroll Indicator */}
            <div className="mt-16 flex justify-center">
                <div className="flex flex-col items-center
                                gap-2 animate-pulse-slow">
                    <p className="text-xs text-slate-600">
                        Scroll to explore
                    </p>
                    <ChevronDown className="w-4 h-4
                                             text-slate-600"/>
                </div>
            </div>
        </div>
    </section>
);

// ==========================================
// FEATURES SECTION
// ==========================================
const FeaturesSection = () => {

    const features = [
        {
            icon:  Brain,
            title: 'AI Risk Scoring',
            desc:  'Our intelligent system automatically analyzes reported threats and assigns risk scores from 0-100 based on multiple factors.',
            color: 'text-purple-400',
            bg:    'bg-purple-500/10',
            border:'border-purple-500/20',
        },
        {
            icon:  ShieldAlert,
            title: 'Instant Reporting',
            desc:  'Students can report suspicious emails, URLs, and incidents in minutes with our streamlined reporting system.',
            color: 'text-red-400',
            bg:    'bg-red-500/10',
            border:'border-red-500/20',
        },
        {
            icon:  Bell,
            title: 'Real-Time Alerts',
            desc:  'Admins publish security alerts that instantly reach all students in the institution with severity-based notifications.',
            color: 'text-yellow-400',
            bg:    'bg-yellow-500/10',
            border:'border-yellow-500/20',
        },
        {
            icon:  BarChart3,
            title: 'Analytics Dashboard',
            desc:  'Comprehensive dashboards show threat trends, risk distributions, and incident statistics for informed decision-making.',
            color: 'text-cyan-400',
            bg:    'bg-cyan-500/10',
            border:'border-cyan-500/20',
        },
        {
            icon:  Users,
            title: 'Role-Based Access',
            desc:  'Separate portals for students and administrators ensure the right people have access to the right information.',
            color: 'text-green-400',
            bg:    'bg-green-500/10',
            border:'border-green-500/20',
        },
        {
            icon:  BookOpen,
            title: 'Security Awareness',
            desc:  'Built-in learning center and awareness resources help students understand and prevent cyber threats.',
            color: 'text-blue-400',
            bg:    'bg-blue-500/10',
            border:'border-blue-500/20',
        },
    ];

    return (
        <section id="features" className="py-24 px-6">
            <div className="max-w-7xl mx-auto">

                {/* Section Header */}
                <div className="text-center mb-16">
                    <div className="inline-flex items-center
                                    gap-2 px-4 py-2
                                    rounded-full
                                    bg-cyan-500/10
                                    border border-cyan-500/20
                                    mb-4">
                        <Zap className="w-3 h-3 text-cyan-400"/>
                        <span className="text-xs text-cyan-400
                                         font-medium">
                            Powerful Features
                        </span>
                    </div>
                    <h2 className="text-4xl font-bold
                                   text-white mb-4">
                        Everything You Need to
                        <span className="text-gradient-blue">
                            {' '}Stay Protected
                        </span>
                    </h2>
                    <p className="text-slate-400
                                  max-w-2xl mx-auto">
                        A complete cybersecurity platform
                        designed specifically for
                        educational institutions.
                    </p>
                </div>

                {/* Features Grid */}
                <div className="grid grid-cols-1
                                md:grid-cols-2
                                lg:grid-cols-3 gap-6">
                    {features.map((feature, i) => (
                        <div key={i}
                             className={`glass-card p-6
                                         border ${feature.border}
                                         hover:scale-105
                                         transition-transform
                                         duration-200`}>
                            <div className={`w-12 h-12
                                             rounded-xl
                                             flex items-center
                                             justify-center
                                             mb-4 ${feature.bg}`}>
                                <feature.icon
                                    className={`w-6 h-6
                                                ${feature.color}`}
                                />
                            </div>
                            <h3 className="text-base font-bold
                                           text-white mb-2">
                                {feature.title}
                            </h3>
                            <p className="text-sm text-slate-400
                                          leading-relaxed">
                                {feature.desc}
                            </p>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

// ==========================================
// HOW IT WORKS SECTION
// ==========================================
const HowItWorksSection = () => {

    const steps = [
        {
            step:  '01',
            icon:  '🎓',
            title: 'Student Registers',
            desc:  'Students create an account with their institution email and join their institution\'s security network.',
            color: 'border-cyan-500/30 bg-cyan-500/5',
        },
        {
            step:  '02',
            icon:  '🚨',
            title: 'Reports Threat',
            desc:  'When suspicious activity is detected, students submit detailed reports with evidence like URLs or emails.',
            color: 'border-red-500/30 bg-red-500/5',
        },
        {
            step:  '03',
            icon:  '🤖',
            title: 'AI Analyzes Risk',
            desc:  'Our AI engine instantly scores the threat from 0-100 and classifies it as Low, Medium, or High risk.',
            color: 'border-purple-500/30 bg-purple-500/5',
        },
        {
            step:  '04',
            icon:  '🛡️',
            title: 'Admin Verifies',
            desc:  'Security administrators review the AI analysis, verify the threat, and take appropriate action.',
            color: 'border-green-500/30 bg-green-500/5',
        },
        {
            step:  '05',
            icon:  '📢',
            title: 'Alert Published',
            desc:  'Verified threats trigger institution-wide security alerts to protect all students immediately.',
            color: 'border-yellow-500/30 bg-yellow-500/5',
        },
    ];

    return (
        <section id="how-it-works"
                 className="py-24 px-6 bg-white/1">
            <div className="max-w-7xl mx-auto">

                {/* Header */}
                <div className="text-center mb-16">
                    <div className="inline-flex items-center
                                    gap-2 px-4 py-2
                                    rounded-full
                                    bg-green-500/10
                                    border border-green-500/20
                                    mb-4">
                        <CheckCircle className="w-3 h-3
                                                 text-green-400"/>
                        <span className="text-xs text-green-400
                                         font-medium">
                            Simple Process
                        </span>
                    </div>
                    <h2 className="text-4xl font-bold
                                   text-white mb-4">
                        How CyberShield
                        <span className="text-gradient-green">
                            {' '}Works
                        </span>
                    </h2>
                    <p className="text-slate-400
                                  max-w-2xl mx-auto">
                        From threat detection to institution
                        protection in 5 simple steps.
                    </p>
                </div>

                {/* Steps */}
                <div className="grid grid-cols-1
                                md:grid-cols-5 gap-4">
                    {steps.map((step, i) => (
                        <div key={i} className="relative">
                            <div className={`glass-card p-5
                                             border ${step.color}
                                             text-center h-full`}>
                                <div className="text-3xl mb-3">
                                    {step.icon}
                                </div>
                                <div className="text-xs font-bold
                                                text-slate-600 mb-2">
                                    STEP {step.step}
                                </div>
                                <h3 className="text-sm font-bold
                                               text-white mb-2">
                                    {step.title}
                                </h3>
                                <p className="text-xs text-slate-400
                                              leading-relaxed">
                                    {step.desc}
                                </p>
                            </div>
                            {i < steps.length - 1 && (
                                <div className="hidden md:block
                                                absolute -right-2
                                                top-1/2
                                                -translate-y-1/2
                                                z-10">
                                    <ArrowRight className="w-4 h-4
                                                            text-slate-600"/>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

// ==========================================
// ROLES SECTION
// ==========================================
const RolesSection = ({ onRegister }) => (
    <section id="about" className="py-24 px-6">
        <div className="max-w-7xl mx-auto">

            <div className="text-center mb-16">
                <h2 className="text-4xl font-bold
                               text-white mb-4">
                    Built for
                    <span className="text-gradient-blue">
                        {' '}Everyone
                    </span>
                </h2>
                <p className="text-slate-400
                              max-w-xl mx-auto">
                    Different roles with tailored
                    experiences for maximum effectiveness.
                </p>
            </div>

            <div className="grid grid-cols-1
                            md:grid-cols-2 gap-8">

                {/* Student Card */}
                <div className="glass-card p-8
                                border border-cyan-500/20">
                    <div className="text-5xl mb-6">🎓</div>
                    <h3 className="text-xl font-bold
                                   text-white mb-3">
                        For Students
                    </h3>
                    <p className="text-slate-400 text-sm
                                  mb-6 leading-relaxed">
                        Empower yourself with the tools to
                        identify and report cyber threats
                        protecting your institution.
                    </p>
                    <ul className="space-y-3 mb-8">
                        {[
                            'Report suspicious emails and URLs',
                            'Track your submitted reports',
                            'Receive real-time security alerts',
                            'Access cybersecurity learning resources',
                            'View AI-powered threat analysis',
                        ].map((item, i) => (
                            <li key={i}
                                className="flex items-center
                                           gap-3">
                                <CheckCircle className="w-4 h-4
                                                         text-cyan-400
                                                         flex-shrink-0"/>
                                <span className="text-sm
                                                 text-slate-300">
                                    {item}
                                </span>
                            </li>
                        ))}
                    </ul>

                    {/* Student Join Button */}
                    <button
                        onClick={onRegister}
                        className="btn-cyber flex items-center
                                   gap-2 px-6 py-3 text-sm
                                   w-full justify-center">
                        <GraduationCap className="w-4 h-4"/>
                        Join as Student
                        <ArrowRight className="w-4 h-4"/>
                    </button>
                </div>

                {/* Admin Card */}
                <div className="glass-card p-8
                                border border-purple-500/20">
                    <div className="text-5xl mb-6">🛡️</div>
                    <h3 className="text-xl font-bold
                                   text-white mb-3">
                        For Administrators
                    </h3>
                    <p className="text-slate-400 text-sm
                                  mb-6 leading-relaxed">
                        Manage your institution's security
                        posture with powerful admin tools
                        and comprehensive analytics.
                    </p>
                    <ul className="space-y-3 mb-8">
                        {[
                            'Review and verify reported threats',
                            'Publish institution-wide alerts',
                            'View comprehensive analytics',
                            'Manage student accounts',
                            'Access complete audit logs',
                        ].map((item, i) => (
                            <li key={i}
                                className="flex items-center
                                           gap-3">
                                <CheckCircle className="w-4 h-4
                                                         text-purple-400
                                                         flex-shrink-0"/>
                                <span className="text-sm
                                                 text-slate-300">
                                    {item}
                                </span>
                            </li>
                        ))}
                    </ul>

                    {/* ✅ Admin Pre-configured Notice */}
                    <div className="flex items-center gap-3
                                    px-6 py-3 rounded-xl
                                    bg-purple-500/5
                                    border border-purple-500/20
                                    w-full">
                        <Lock className="w-4 h-4
                                         text-purple-400
                                         flex-shrink-0"/>
                        <div className="text-left">
                            <p className="text-sm font-medium
                                          text-purple-400">
                                Admin Access
                            </p>
                            <p className="text-xs
                                          text-slate-500">
                                Accounts are pre-configured
                                by the system
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>
);

// ==========================================
// CTA SECTION
// ==========================================
const CTASection = ({ onRegister }) => (
    <section className="py-24 px-6">
        <div className="max-w-4xl mx-auto text-center">
            <div className="glass-card p-12
                            border border-cyan-500/20
                            relative overflow-hidden">

                <div className="absolute top-0 left-0
                                w-full h-full
                                bg-gradient-to-br
                                from-cyan-500/5
                                to-blue-500/5"/>

                <div className="relative">
                    <div className="w-16 h-16 rounded-2xl
                                    bg-cyan-500/20
                                    border border-cyan-500/30
                                    flex items-center
                                    justify-center
                                    mx-auto mb-6">
                        <Shield className="w-8 h-8
                                           text-cyan-400"/>
                    </div>
                    <h2 className="text-4xl font-bold
                                   text-white mb-4">
                        Ready to Secure Your
                        <span className="text-gradient-blue">
                            {' '}Institution?
                        </span>
                    </h2>
                    <p className="text-slate-400 mb-8
                                  max-w-xl mx-auto">
                        Join CyberShield today and build
                        a safer digital environment for
                        your educational institution.
                    </p>
                    <button
                        onClick={onRegister}
                        className="btn-cyber flex items-center
                                   gap-2 px-8 py-4 text-base
                                   font-semibold mx-auto">
                        <GraduationCap className="w-5 h-5"/>
                        Create Student Account Free
                        <ArrowRight className="w-4 h-4"/>
                    </button>
                </div>
            </div>
        </div>
    </section>
);

// ==========================================
// FOOTER
// ==========================================
const Footer = () => (
    <footer className="py-8 px-6 border-t border-white/5">
        <div className="max-w-7xl mx-auto
                        flex flex-col md:flex-row
                        items-center justify-between gap-4">
            <div className="flex items-center gap-2">
                <div className="w-7 h-7 rounded-lg
                                bg-cyan-500/20
                                border border-cyan-500/30
                                flex items-center
                                justify-center">
                    <Shield className="w-4 h-4 text-cyan-400"/>
                </div>
                <span className="text-sm font-bold text-white">
                    CyberShield
                </span>
            </div>
            <p className="text-xs text-slate-500 text-center">
                © 2024 CyberShield —
                AI-Driven Cyber Threat Monitoring
                and Awareness System for
                Educational Institutions
            </p>
            <div className="flex items-center gap-1">
                <div className="w-2 h-2 rounded-full
                                bg-green-400 animate-pulse"/>
                <span className="text-xs text-green-400">
                    All Systems Operational
                </span>
            </div>
        </div>
    </footer>
);

// ==========================================
// LANDING PAGE MAIN COMPONENT
// ==========================================
const LandingPage = () => {

    const navigate = useNavigate();

    const handleLogin    = () => navigate('/login');
    const handleRegister = () => navigate('/register');

    return (
        <div className="min-h-screen bg-cyber-gradient">
            <LandingNavbar
                onLogin={handleLogin}
                onRegister={handleRegister}
            />
            <HeroSection
                onRegister={handleRegister}
                onLogin={handleLogin}
            />
            <FeaturesSection/>
            <HowItWorksSection/>
            <RolesSection onRegister={handleRegister}/>
            <CTASection   onRegister={handleRegister}/>
            <Footer/>
        </div>
    );
};

export default LandingPage;
