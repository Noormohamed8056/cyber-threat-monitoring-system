import React, { useState } from 'react';
import { useNavigate }      from 'react-router-dom';
import {
    GraduationCap,
    BookOpen,
    Play,
    CheckCircle,
    Clock,
    ArrowLeft,
    ArrowRight,
    Star,
    Shield,
    Lock,
    AlertTriangle,
    Wifi,
    Smartphone,
    Globe,
    Award,
    ChevronRight,
} from 'lucide-react';

// ==========================================
// COURSES DATA
// ==========================================
const courses = [
    {
        id:       1,
        icon:     '🛡️',
        title:    'Cybersecurity Fundamentals',
        desc:     'Learn the core concepts of cybersecurity, common threats, and basic protection strategies.',
        level:    'Beginner',
        duration: '45 min',
        modules:  8,
        rating:   4.9,
        color:    'border-cyan-500/20',
        bgColor:  'bg-cyan-500/5',
        iconBg:   'bg-cyan-500/10',
        topics: [
            'What is Cybersecurity?',
            'Common Threat Types',
            'CIA Triad (Confidentiality, Integrity, Availability)',
            'Security Best Practices',
            'Introduction to Risk Management',
        ],
    },
    {
        id:       2,
        icon:     '🎣',
        title:    'Phishing Detection Mastery',
        desc:     'Master the art of identifying phishing emails, fake websites, and social engineering attacks.',
        level:    'Beginner',
        duration: '30 min',
        modules:  6,
        rating:   4.8,
        color:    'border-red-500/20',
        bgColor:  'bg-red-500/5',
        iconBg:   'bg-red-500/10',
        topics: [
            'Anatomy of a Phishing Email',
            'URL Inspection Techniques',
            'Identifying Fake Websites',
            'Social Engineering Red Flags',
            'Reporting Phishing Attempts',
        ],
    },
    {
        id:       3,
        icon:     '🔐',
        title:    'Password Security & MFA',
        desc:     'Create unbreakable passwords and set up multi-factor authentication to secure your accounts.',
        level:    'Beginner',
        duration: '25 min',
        modules:  5,
        rating:   4.7,
        color:    'border-green-500/20',
        bgColor:  'bg-green-500/5',
        iconBg:   'bg-green-500/10',
        topics: [
            'Password Strength Principles',
            'Using Password Managers',
            'Two-Factor Authentication Setup',
            'Biometric Security',
            'Account Recovery Best Practices',
        ],
    },
    {
        id:       4,
        icon:     '🔒',
        title:    'Ransomware Prevention',
        desc:     'Understand ransomware attacks and learn how to protect your data from encryption threats.',
        level:    'Intermediate',
        duration: '40 min',
        modules:  7,
        rating:   4.9,
        color:    'border-purple-500/20',
        bgColor:  'bg-purple-500/5',
        iconBg:   'bg-purple-500/10',
        topics: [
            'How Ransomware Spreads',
            'Backup Strategies',
            'Safe Browsing Habits',
            'Email Attachment Safety',
            'Recovery Planning',
        ],
    },
    {
        id:       5,
        icon:     '📡',
        title:    'Safe Internet & WiFi Usage',
        desc:     'Navigate the internet safely and protect yourself on public and private networks.',
        level:    'Beginner',
        duration: '20 min',
        modules:  4,
        rating:   4.6,
        color:    'border-blue-500/20',
        bgColor:  'bg-blue-500/5',
        iconBg:   'bg-blue-500/10',
        topics: [
            'Public WiFi Risks',
            'VPN Usage Guide',
            'HTTPS and SSL Certificates',
            'Browser Security Settings',
        ],
    },
    {
        id:       6,
        icon:     '📱',
        title:    'Mobile Device Security',
        desc:     'Secure your smartphone and tablet against mobile-specific threats and app-based attacks.',
        level:    'Intermediate',
        duration: '35 min',
        modules:  6,
        rating:   4.7,
        color:    'border-yellow-500/20',
        bgColor:  'bg-yellow-500/5',
        iconBg:   'bg-yellow-500/10',
        topics: [
            'App Permission Management',
            'Mobile Malware Prevention',
            'Secure Messaging Apps',
            'Device Encryption',
            'Lost Device Protocols',
        ],
    },
];

// ==========================================
// QUICK LESSONS DATA
// ==========================================
const quickLessons = [
    {
        icon:  Lock,
        title: 'Lock Your Screen',
        time:  '2 min',
        color: 'text-cyan-400',
        bg:    'bg-cyan-500/10',
        content: 'Always lock your screen when leaving your device. Use Win+L on Windows or Cmd+Ctrl+Q on Mac.',
    },
    {
        icon:  AlertTriangle,
        title: 'Spot Fake Emails',
        time:  '3 min',
        color: 'text-red-400',
        bg:    'bg-red-500/10',
        content: 'Check sender email carefully. Look for misspellings in domain names like "paypa1.com" instead of "paypal.com".',
    },
    {
        icon:  Wifi,
        title: 'Public WiFi Safety',
        time:  '2 min',
        color: 'text-yellow-400',
        bg:    'bg-yellow-500/10',
        content: 'Never access banking or sensitive accounts on public WiFi. Use your mobile data or a VPN instead.',
    },
    {
        icon:  Smartphone,
        title: 'App Permissions',
        time:  '2 min',
        color: 'text-purple-400',
        bg:    'bg-purple-500/10',
        content: 'Review app permissions before installing. A flashlight app should never need access to your contacts.',
    },
    {
        icon:  Globe,
        title: 'Safe Browsing',
        time:  '3 min',
        color: 'text-green-400',
        bg:    'bg-green-500/10',
        content: 'Always check for HTTPS and the padlock icon before entering any personal information on a website.',
    },
    {
        icon:  Shield,
        title: 'Update Software',
        time:  '2 min',
        color: 'text-blue-400',
        bg:    'bg-blue-500/10',
        content: 'Keep all software updated. Most cyberattacks exploit known vulnerabilities that patches already fix.',
    },
];

// ==========================================
// COURSE CARD COMPONENT
// ==========================================
const CourseCard = ({ course, onStart }) => {

    const [isExpanded, setIsExpanded] = useState(false);

    const getLevelColor = (level) => {
        return level === 'Beginner'
            ? 'text-green-400 bg-green-500/10 border-green-500/20'
            : 'text-yellow-400 bg-yellow-500/10 border-yellow-500/20';
    };

    return (
        <div className={`glass-card border
                         ${course.color}
                         ${course.bgColor}
                         overflow-hidden
                         hover:scale-[1.01]
                         transition-all duration-200`}>

            {/* Card Header */}
            <div className="p-5">
                <div className="flex items-start
                                justify-between mb-4">
                    <div className={`w-12 h-12 rounded-xl
                                     flex items-center
                                     justify-center
                                     text-2xl
                                     ${course.iconBg}`}>
                        {course.icon}
                    </div>
                    <span className={`px-2 py-1 rounded-full
                                      text-xs font-medium
                                      border
                                      ${getLevelColor(
                                          course.level
                                      )}`}>
                        {course.level}
                    </span>
                </div>

                <h3 className="text-base font-bold
                               text-white mb-2">
                    {course.title}
                </h3>
                <p className="text-xs text-slate-400
                              leading-relaxed mb-4">
                    {course.desc}
                </p>

                {/* Meta Info */}
                <div className="flex items-center
                                gap-4 text-xs
                                text-slate-500 mb-4">
                    <div className="flex items-center gap-1">
                        <Clock className="w-3 h-3"/>
                        {course.duration}
                    </div>
                    <div className="flex items-center gap-1">
                        <BookOpen className="w-3 h-3"/>
                        {course.modules} modules
                    </div>
                    <div className="flex items-center gap-1">
                        <Star className="w-3 h-3
                                         text-yellow-400
                                         fill-yellow-400"/>
                        {course.rating}
                    </div>
                </div>

                {/* Topics Toggle */}
                <button
                    onClick={() =>
                        setIsExpanded(!isExpanded)
                    }
                    className="w-full flex items-center
                               justify-between py-2
                               text-xs text-slate-400
                               hover:text-cyan-400
                               transition-colors">
                    <span>View Topics</span>
                    <ChevronRight
                        className={`w-3 h-3 transition-transform
                                    ${isExpanded
                                        ? 'rotate-90'
                                        : ''
                                    }`}
                    />
                </button>

                {/* Topics List */}
                {isExpanded && (
                    <div className="mt-2 space-y-1.5
                                    border-t border-white/5
                                    pt-3">
                        {course.topics.map((topic, i) => (
                            <div key={i}
                                 className="flex items-center
                                            gap-2">
                                <CheckCircle className="w-3 h-3
                                                         text-cyan-400
                                                         flex-shrink-0"/>
                                <span className="text-xs
                                                 text-slate-300">
                                    {topic}
                                </span>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Start Button */}
            <div className="px-5 pb-5">
                <button
                    onClick={() => onStart(course)}
                    className="w-full btn-cyber py-2.5
                               text-sm flex items-center
                               justify-center gap-2">
                    <Play className="w-4 h-4"/>
                    Start Course
                </button>
            </div>
        </div>
    );
};

// ==========================================
// COURSE MODAL
// ==========================================
const CourseModal = ({ course, onClose }) => {

    const [currentLesson, setCurrentLesson] = useState(0);
    const [completed,     setCompleted]     = useState([]);

    const handleComplete = (index) => {
        if (!completed.includes(index)) {
            setCompleted([...completed, index]);
        }
        if (index < course.topics.length - 1) {
            setCurrentLesson(index + 1);
        }
    };

    const progress = Math.round(
        (completed.length / course.topics.length) * 100
    );

    return (
        <div className="fixed inset-0 z-50
                        flex items-center justify-center
                        p-4"
             style={{
                 background: 'rgba(0,0,0,0.8)',
                 backdropFilter: 'blur(4px)',
             }}>
            <div className="glass-card w-full max-w-2xl
                            max-h-[90vh] overflow-y-auto
                            border border-white/10">

                {/* Header */}
                <div className="flex items-center
                                justify-between p-6
                                border-b border-white/5">
                    <div className="flex items-center gap-3">
                        <div className="text-2xl">
                            {course.icon}
                        </div>
                        <div>
                            <h2 className="text-base
                                           font-bold
                                           text-white">
                                {course.title}
                            </h2>
                            <p className="text-xs
                                          text-slate-400">
                                {completed.length}/
                                {course.topics.length}
                                {' '}lessons completed
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-8 h-8 rounded-lg
                                   flex items-center
                                   justify-center
                                   text-slate-400
                                   hover:text-white
                                   hover:bg-white/5
                                   transition-all">
                        ✕
                    </button>
                </div>

                {/* Progress Bar */}
                <div className="px-6 py-4
                                border-b border-white/5">
                    <div className="flex items-center
                                    justify-between
                                    text-xs mb-2">
                        <span className="text-slate-400">
                            Progress
                        </span>
                        <span className="text-cyan-400
                                         font-semibold">
                            {progress}%
                        </span>
                    </div>
                    <div className="w-full h-2 rounded-full
                                    bg-white/10">
                        <div
                            className="h-2 rounded-full
                                       bg-gradient-to-r
                                       from-cyan-500
                                       to-blue-500
                                       transition-all
                                       duration-500"
                            style={{
                                width: `${progress}%`
                            }}
                        />
                    </div>
                </div>

                {/* Content */}
                <div className="p-6">

                    {/* Completed Banner */}
                    {progress === 100 && (
                        <div className="mb-6 p-4 rounded-xl
                                        bg-green-500/10
                                        border
                                        border-green-500/30
                                        text-center">
                            <Award className="w-8 h-8
                                              text-yellow-400
                                              mx-auto mb-2"/>
                            <p className="text-sm font-bold
                                          text-white">
                                🎉 Course Completed!
                            </p>
                            <p className="text-xs
                                          text-slate-400 mt-1">
                                Excellent work! You have
                                mastered {course.title}
                            </p>
                        </div>
                    )}

                    {/* Lessons List */}
                    <div className="space-y-3">
                        {course.topics.map((topic, i) => (
                            <div
                                key={i}
                                onClick={() =>
                                    setCurrentLesson(i)
                                }
                                className={`p-4 rounded-xl
                                            border cursor-pointer
                                            transition-all
                                            ${currentLesson === i
                                                ? 'border-cyan-500/40 bg-cyan-500/5'
                                                : completed.includes(i)
                                                ? 'border-green-500/20 bg-green-500/3'
                                                : 'border-white/5 bg-white/2 hover:border-white/10'
                                            }`}>
                                <div className="flex items-center
                                                justify-between">
                                    <div className="flex items-center
                                                    gap-3">
                                        <div className={`w-7 h-7
                                                         rounded-full
                                                         flex items-center
                                                         justify-center
                                                         text-xs font-bold
                                                         ${completed.includes(i)
                                                             ? 'bg-green-500 text-white'
                                                             : currentLesson === i
                                                             ? 'bg-cyan-500 text-dark-500'
                                                             : 'bg-white/10 text-slate-400'
                                                         }`}>
                                            {completed.includes(i)
                                                ? '✓'
                                                : i + 1
                                            }
                                        </div>
                                        <span className={`text-sm
                                                          font-medium
                                                          ${completed.includes(i)
                                                              ? 'text-green-400 line-through opacity-70'
                                                              : currentLesson === i
                                                              ? 'text-cyan-400'
                                                              : 'text-slate-300'
                                                          }`}>
                                            {topic}
                                        </span>
                                    </div>

                                    {currentLesson === i
                                     && !completed.includes(i)
                                     && (
                                        <button
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleComplete(i);
                                            }}
                                            className="px-3 py-1.5
                                                       rounded-lg
                                                       bg-cyan-500/20
                                                       border
                                                       border-cyan-500/30
                                                       text-cyan-400
                                                       text-xs
                                                       font-medium
                                                       hover:bg-cyan-500/30
                                                       transition-all">
                                            Mark Done ✓
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

// ==========================================
// LEARNING CENTER PAGE
// ==========================================
const LearningCenter = () => {

    const navigate = useNavigate();
    const [selectedCourse,
           setSelectedCourse] = useState(null);
    const [activeTab,
           setActiveTab]      = useState('courses');

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
                        Learning Center
                    </h1>
                    <p className="text-slate-400 text-sm">
                        Master cybersecurity at your
                        own pace
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
                <div className="relative flex items-center
                                justify-between">
                    <div>
                        <div className="flex items-center
                                        gap-2 mb-2">
                            <GraduationCap className="w-5 h-5
                                                       text-cyan-400"/>
                            <h2 className="text-lg font-bold
                                           text-white">
                                Your Security Journey
                            </h2>
                        </div>
                        <p className="text-sm text-slate-400
                                      max-w-lg">
                            Learn essential cybersecurity
                            skills through interactive courses
                            and quick lessons designed for
                            students.
                        </p>
                    </div>
                    <div className="hidden md:flex
                                    items-center gap-6
                                    text-center">
                        {[
                            {
                                value: '6',
                                label: 'Courses'
                            },
                            {
                                value: '6',
                                label: 'Quick Lessons'
                            },
                            {
                                value: 'Free',
                                label: 'Always'
                            },
                        ].map((stat, i) => (
                            <div key={i}>
                                <p className="text-xl
                                              font-bold
                                              text-cyan-400">
                                    {stat.value}
                                </p>
                                <p className="text-xs
                                              text-slate-500">
                                    {stat.label}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Tabs */}
            <div className="flex items-center gap-1
                            p-1 rounded-xl bg-white/3
                            border border-white/5 w-fit">
                {[
                    {
                        key:   'courses',
                        label: '📚 Courses',
                    },
                    {
                        key:   'quick',
                        label: '⚡ Quick Lessons',
                    },
                ].map(tab => (
                    <button
                        key={tab.key}
                        onClick={() =>
                            setActiveTab(tab.key)
                        }
                        className={`px-5 py-2 rounded-lg
                                    text-sm font-medium
                                    transition-all
                                    ${activeTab === tab.key
                                        ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30'
                                        : 'text-slate-400 hover:text-slate-200'
                                    }`}>
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Courses Tab */}
            {activeTab === 'courses' && (
                <div>
                    <div className="flex items-center
                                    justify-between mb-4">
                        <p className="text-sm text-slate-400">
                            {courses.length} courses available
                        </p>
                    </div>
                    <div className="grid grid-cols-1
                                    md:grid-cols-2
                                    lg:grid-cols-3 gap-5">
                        {courses.map(course => (
                            <CourseCard
                                key={course.id}
                                course={course}
                                onStart={setSelectedCourse}
                            />
                        ))}
                    </div>
                </div>
            )}

            {/* Quick Lessons Tab */}
            {activeTab === 'quick' && (
                <div>
                    <div className="mb-4">
                        <p className="text-sm text-slate-400">
                            Short focused lessons —
                            learn something new in minutes
                        </p>
                    </div>
                    <div className="grid grid-cols-1
                                    md:grid-cols-2 gap-4">
                        {quickLessons.map((lesson, i) => (
                            <div key={i}
                                 className="glass-card p-5
                                            border border-white/5
                                            hover:border-cyan-500/20
                                            transition-all">
                                <div className="flex items-start
                                                gap-4">
                                    <div className={`w-10 h-10
                                                     rounded-xl
                                                     flex items-center
                                                     justify-center
                                                     flex-shrink-0
                                                     ${lesson.bg}`}>
                                        <lesson.icon
                                            className={`w-5 h-5
                                                        ${lesson.color}`}
                                        />
                                    </div>
                                    <div className="flex-1">
                                        <div className="flex items-center
                                                        justify-between
                                                        mb-2">
                                            <h3 className="text-sm
                                                           font-semibold
                                                           text-white">
                                                {lesson.title}
                                            </h3>
                                            <span className="text-xs
                                                             text-slate-500
                                                             flex items-center
                                                             gap-1">
                                                <Clock className="w-3 h-3"/>
                                                {lesson.time}
                                            </span>
                                        </div>
                                        <p className="text-xs
                                                      text-slate-400
                                                      leading-relaxed">
                                            {lesson.content}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Course Modal */}
            {selectedCourse && (
                <CourseModal
                    course={selectedCourse}
                    onClose={() =>
                        setSelectedCourse(null)
                    }
                />
            )}
        </div>
    );
};

export default LearningCenter;