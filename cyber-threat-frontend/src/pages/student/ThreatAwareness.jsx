import React, { useState } from 'react';
import { useNavigate }      from 'react-router-dom';
import {
    BookOpen,
    Shield,
    AlertTriangle,
    Lock,
    Mail,
    Link,
    Smartphone,
    Wifi,
    Eye,
    Key,
    ArrowLeft,
    ChevronDown,
    ChevronUp,
    CheckCircle,
    ExternalLink,
} from 'lucide-react';

// ==========================================
// AWARENESS DATA
// ==========================================
const categories = [
    {
        id:    'phishing',
        icon:  '🎣',
        title: 'Phishing Attacks',
        color: 'border-red-500/20 bg-red-500/5',
        iconColor: 'text-red-400',
        iconBg:    'bg-red-500/10',
        description:
            'Phishing is a cybercrime where attackers trick you into revealing sensitive information by pretending to be trustworthy entities.',
        tips: [
            'Check sender email address carefully — look for slight misspellings',
            'Never click links in unexpected emails — go directly to the website',
            'Legitimate organizations never ask for passwords via email',
            'Look for HTTPS and padlock icon before entering any credentials',
            'When in doubt, call the organization directly to verify',
        ],
        warning:
            'If you receive a suspicious email, do NOT click any links. Report it immediately using the Report Threat feature.',
        examples: [
            'Fake bank emails asking to verify account',
            'IT department asking for password reset',
            'Prize winning notifications with links',
            'Urgent security alert from unknown sender',
        ],
    },
    {
        id:    'malware',
        icon:  '🦠',
        title: 'Malware & Viruses',
        color: 'border-orange-500/20 bg-orange-500/5',
        iconColor: 'text-orange-400',
        iconBg:    'bg-orange-500/10',
        description:
            'Malware is malicious software designed to damage, disrupt, or gain unauthorized access to computer systems.',
        tips: [
            'Never download software from untrusted sources',
            'Keep your operating system and antivirus updated',
            'Avoid clicking on pop-up advertisements',
            'Scan USB drives before opening files',
            'Be cautious of email attachments even from known contacts',
        ],
        warning:
            'If your computer behaves unusually — slow performance, unexpected pop-ups, or unknown programs — report it immediately.',
        examples: [
            'Fake software update installers',
            'Email attachments with .exe or .zip files',
            'Infected USB drives',
            'Malicious browser extensions',
        ],
    },
    {
        id:    'ransomware',
        icon:  '🔒',
        title: 'Ransomware',
        color: 'border-purple-500/20 bg-purple-500/5',
        iconColor: 'text-purple-400',
        iconBg:    'bg-purple-500/10',
        description:
            'Ransomware encrypts your files and demands payment for decryption. It can spread across entire networks.',
        tips: [
            'Backup important files regularly to external drives or cloud',
            'Never open unexpected email attachments',
            'Keep all software updated with security patches',
            'Use strong antivirus software with ransomware protection',
            'Never pay the ransom — it does not guarantee file recovery',
        ],
        warning:
            'If you see a ransom note or cannot access your files, immediately disconnect from the network and report to IT.',
        examples: [
            'WannaCry style encrypted file messages',
            'Files renamed with unknown extensions',
            'Desktop background changed to ransom note',
            'Unable to open documents suddenly',
        ],
    },
    {
        id:    'passwords',
        icon:  '🔑',
        title: 'Password Security',
        color: 'border-cyan-500/20 bg-cyan-500/5',
        iconColor: 'text-cyan-400',
        iconBg:    'bg-cyan-500/10',
        description:
            'Weak passwords are one of the most common ways attackers gain unauthorized access to accounts and systems.',
        tips: [
            'Use at least 12 characters with letters, numbers and symbols',
            'Never reuse the same password across multiple sites',
            'Enable two-factor authentication wherever available',
            'Use a password manager to generate and store strong passwords',
            'Change passwords immediately if you suspect a breach',
        ],
        warning:
            'Never share your password with anyone — including IT staff. Legitimate support never needs your password.',
        examples: [
            'Using "password123" or "admin" as password',
            'Using same password for email and banking',
            'Sharing passwords with classmates',
            'Writing passwords on sticky notes',
        ],
    },
    {
        id:    'social',
        icon:  '👥',
        title: 'Social Engineering',
        color: 'border-yellow-500/20 bg-yellow-500/5',
        iconColor: 'text-yellow-400',
        iconBg:    'bg-yellow-500/10',
        description:
            'Social engineering manipulates people into giving up confidential information through psychological tricks.',
        tips: [
            'Be suspicious of urgent requests for information',
            'Verify identity of anyone requesting sensitive data',
            'Do not share personal information on social media',
            'Be cautious of people claiming to be IT support',
            'Trust your instincts — if something feels wrong, report it',
        ],
        warning:
            'Attackers often impersonate authority figures. Always verify identity through official channels.',
        examples: [
            'Fake IT support calling to fix your computer',
            'Someone claiming to be professor asking for login',
            'Suspicious person asking about network access',
            'Fake surveys collecting personal data',
        ],
    },
    {
        id:    'wifi',
        icon:  '📡',
        title: 'Public WiFi Risks',
        color: 'border-green-500/20 bg-green-500/5',
        iconColor: 'text-green-400',
        iconBg:    'bg-green-500/10',
        description:
            'Public WiFi networks are often unsecured and can be used by attackers to intercept your data.',
        tips: [
            'Avoid accessing sensitive accounts on public WiFi',
            'Use a VPN when connecting to public networks',
            'Turn off automatic WiFi connection on your device',
            'Verify network name with staff before connecting',
            'Log out of accounts after using public computers',
        ],
        warning:
            'Never perform banking or enter passwords on public WiFi without a VPN.',
        examples: [
            'Using cafe WiFi to access bank account',
            'Connecting to "Free_Airport_WiFi" unknowingly',
            'Evil twin networks mimicking campus WiFi',
            'Man-in-the-middle attacks on open networks',
        ],
    },
];

// ==========================================
// QUICK TIPS DATA
// ==========================================
const quickTips = [
    {
        icon:  Lock,
        title: 'Lock Your Screen',
        tip:   'Always lock your computer when stepping away. Press Win+L or Cmd+Ctrl+Q.',
        color: 'text-cyan-400',
        bg:    'bg-cyan-500/10',
    },
    {
        icon:  Mail,
        title: 'Think Before You Click',
        tip:   'Hover over links to preview URLs. If it looks suspicious, do not click.',
        color: 'text-yellow-400',
        bg:    'bg-yellow-500/10',
    },
    {
        icon:  Key,
        title: 'Two-Factor Auth',
        tip:   'Enable 2FA on all important accounts for an extra layer of security.',
        color: 'text-green-400',
        bg:    'bg-green-500/10',
    },
    {
        icon:  Eye,
        title: 'Stay Alert',
        tip:   'Report anything suspicious immediately. Early reporting prevents major incidents.',
        color: 'text-red-400',
        bg:    'bg-red-500/10',
    },
];

// ==========================================
// CATEGORY CARD COMPONENT
// ==========================================
const CategoryCard = ({ category }) => {

    const [isExpanded, setIsExpanded] = useState(false);

    return (
        <div className={`glass-card border
                         ${category.color}
                         overflow-hidden
                         transition-all duration-300`}>

            {/* Header */}
            <button
                onClick={() =>
                    setIsExpanded(!isExpanded)
                }
                className="w-full p-5 flex items-center
                           justify-between text-left">
                <div className="flex items-center gap-4">
                    <div className={`w-12 h-12 rounded-xl
                                     flex items-center
                                     justify-center
                                     text-2xl
                                     ${category.iconBg}`}>
                        {category.icon}
                    </div>
                    <div>
                        <h3 className="text-base font-bold
                                       text-white">
                            {category.title}
                        </h3>
                        <p className="text-xs
                                      text-slate-400 mt-0.5">
                            Click to learn more
                        </p>
                    </div>
                </div>
                {isExpanded
                    ? <ChevronUp className="w-5 h-5
                                            text-slate-400
                                            flex-shrink-0"/>
                    : <ChevronDown className="w-5 h-5
                                               text-slate-400
                                               flex-shrink-0"/>
                }
            </button>

            {/* Expanded Content */}
            {isExpanded && (
                <div className="px-5 pb-5 space-y-4
                                border-t border-white/5
                                pt-4">

                    {/* Description */}
                    <p className="text-sm text-slate-300
                                  leading-relaxed">
                        {category.description}
                    </p>

                    {/* Tips */}
                    <div>
                        <p className="text-xs font-semibold
                                      text-white uppercase
                                      tracking-wider mb-3">
                            🛡️ Protection Tips
                        </p>
                        <div className="space-y-2">
                            {category.tips.map((tip, i) => (
                                <div key={i}
                                     className="flex items-start
                                                gap-3">
                                    <CheckCircle
                                        className={`w-4 h-4
                                                    flex-shrink-0
                                                    mt-0.5
                                                    ${category.iconColor}`}
                                    />
                                    <p className="text-xs
                                                  text-slate-300
                                                  leading-relaxed">
                                        {tip}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Common Examples */}
                    <div>
                        <p className="text-xs font-semibold
                                      text-white uppercase
                                      tracking-wider mb-3">
                            ⚠️ Common Examples
                        </p>
                        <div className="grid grid-cols-1
                                        sm:grid-cols-2 gap-2">
                            {category.examples.map(
                                (example, i) => (
                                <div key={i}
                                     className="flex items-start
                                                gap-2 p-2
                                                rounded-lg
                                                bg-white/3">
                                    <span className="text-xs
                                                     text-slate-500
                                                     mt-0.5">
                                        •
                                    </span>
                                    <p className="text-xs
                                                  text-slate-400">
                                        {example}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Warning */}
                    <div className="p-3 rounded-xl
                                    bg-yellow-500/5
                                    border
                                    border-yellow-500/20
                                    flex items-start gap-3">
                        <AlertTriangle className="w-4 h-4
                                                   text-yellow-400
                                                   flex-shrink-0
                                                   mt-0.5"/>
                        <p className="text-xs
                                      text-yellow-300
                                      leading-relaxed">
                            {category.warning}
                        </p>
                    </div>
                </div>
            )}
        </div>
    );
};

// ==========================================
// THREAT AWARENESS PAGE
// ==========================================
const ThreatAwareness = () => {

    const navigate = useNavigate();

    return (
        <div className="space-y-6 animate-fade-in">

            {/* Header */}
            <div className="flex items-center gap-4">
                <button
                    onClick={() => navigate(-1)}
                    className="w-9 h-9 rounded-xl
                               flex items-center
                               justify-center
                               bg-white/5
                               border border-white/10
                               text-slate-400
                               hover:text-cyan-400
                               transition-all">
                    <ArrowLeft className="w-4 h-4"/>
                </button>
                <div>
                    <h1 className="text-2xl font-bold
                                   text-white">
                        Threat Awareness
                    </h1>
                    <p className="text-slate-400 text-sm">
                        Learn to identify and prevent
                        cyber threats
                    </p>
                </div>
            </div>

            {/* Hero Banner */}
            <div className="glass-card p-6
                            border border-cyan-500/10
                            relative overflow-hidden">
                <div className="absolute top-0 right-0
                                w-48 h-48 rounded-full
                                bg-cyan-500/5 blur-3xl"/>
                <div className="relative">
                    <div className="flex items-center
                                    gap-3 mb-3">
                        <div className="w-12 h-12 rounded-xl
                                        bg-cyan-500/10
                                        border
                                        border-cyan-500/20
                                        flex items-center
                                        justify-center">
                            <BookOpen className="w-6 h-6
                                                 text-cyan-400"/>
                        </div>
                        <div>
                            <h2 className="text-lg font-bold
                                           text-white">
                                Stay Cyber Safe
                            </h2>
                            <p className="text-xs
                                          text-slate-400">
                                Your first line of defense
                                is awareness
                            </p>
                        </div>
                    </div>
                    <p className="text-sm text-slate-300
                                  leading-relaxed max-w-2xl">
                        Cybersecurity threats are constantly
                        evolving. Understanding common attack
                        methods helps you recognize and avoid
                        them. Explore each category below to
                        learn how to protect yourself and
                        your institution.
                    </p>
                </div>
            </div>

            {/* Quick Tips Row */}
            <div>
                <h2 className="text-sm font-semibold
                               text-white mb-4
                               flex items-center gap-2">
                    <Shield className="w-4 h-4
                                       text-cyan-400"/>
                    Quick Security Tips
                </h2>
                <div className="grid grid-cols-2
                                lg:grid-cols-4 gap-4">
                    {quickTips.map((tip, i) => (
                        <div key={i}
                             className="glass-card p-4
                                        border border-white/5
                                        hover:border-cyan-500/20
                                        transition-all">
                            <div className={`w-9 h-9
                                             rounded-xl
                                             flex items-center
                                             justify-center
                                             mb-3 ${tip.bg}`}>
                                <tip.icon
                                    className={`w-4 h-4
                                                ${tip.color}`}
                                />
                            </div>
                            <p className="text-xs font-semibold
                                          text-white mb-1">
                                {tip.title}
                            </p>
                            <p className="text-xs
                                          text-slate-400
                                          leading-relaxed">
                                {tip.tip}
                            </p>
                        </div>
                    ))}
                </div>
            </div>

            {/* Threat Categories */}
            <div>
                <h2 className="text-sm font-semibold
                               text-white mb-4
                               flex items-center gap-2">
                    <AlertTriangle className="w-4 h-4
                                               text-yellow-400"/>
                    Threat Categories
                    <span className="text-xs
                                     text-slate-500
                                     font-normal">
                        (click to expand)
                    </span>
                </h2>
                <div className="space-y-3">
                    {categories.map(category => (
                        <CategoryCard
                            key={category.id}
                            category={category}
                        />
                    ))}
                </div>
            </div>

            {/* Emergency Contact */}
            <div className="glass-card p-6
                            border border-red-500/20
                            bg-red-500/3">
                <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-xl
                                    bg-red-500/10
                                    border border-red-500/20
                                    flex items-center
                                    justify-center
                                    flex-shrink-0">
                        <AlertTriangle className="w-6 h-6
                                                   text-red-400"/>
                    </div>
                    <div className="flex-1">
                        <h3 className="text-base font-bold
                                       text-white mb-2">
                            🚨 Spotted a Threat?
                        </h3>
                        <p className="text-sm text-slate-300
                                      leading-relaxed mb-4">
                            If you encounter any suspicious
                            activity, do not hesitate to
                            report it immediately. Early
                            reporting helps protect your
                            entire institution.
                        </p>
                        <button
                            onClick={() =>
                                navigate(
                                    '/student/report-threat'
                                )
                            }
                            className="btn-danger flex
                                       items-center gap-2
                                       px-5 py-2.5 text-sm">
                            <Shield className="w-4 h-4"/>
                            Report a Threat Now
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ThreatAwareness;