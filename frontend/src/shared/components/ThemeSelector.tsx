import React from 'react';
import { useTheme } from '../hooks';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { ThemeName } from '../../types/theme';

interface ThemePreviewProps {
  themeName: ThemeName;
  isActive: boolean;
  onClick: () => void;
}

const ThemePreview: React.FC<ThemePreviewProps> = ({ themeName, isActive, onClick }) => {
  const { themes } = useTheme();
  const theme = themes[themeName];
  
  if (!theme) return null;

  const lightColors = theme.cssVars.light;
  
  return (
    <Card 
      className={`cursor-pointer transition-all duration-200 hover:scale-105 ${
        isActive ? 'ring-2 ring-primary shadow-lg' : 'hover:shadow-md'
      }`}
      onClick={onClick}
    >
      <CardHeader className="pb-3">
        <CardTitle className="text-sm flex items-center justify-between">
          {theme.name.split('-').map(word => 
            word.charAt(0).toUpperCase() + word.slice(1)
          ).join(' ')}
          {isActive && <Badge variant="default" className="ml-2">활성</Badge>}
        </CardTitle>
      </CardHeader>
      <CardContent className="pt-0">
        {/* Color Preview */}
        <div className="grid grid-cols-6 gap-1 mb-3">
          <div 
            className="w-6 h-6 rounded-sm border"
            style={{ backgroundColor: lightColors.primary }}
            title="Primary"
          />
          <div 
            className="w-6 h-6 rounded-sm border"
            style={{ backgroundColor: lightColors.secondary }}
            title="Secondary"
          />
          <div 
            className="w-6 h-6 rounded-sm border"
            style={{ backgroundColor: lightColors.accent }}
            title="Accent"
          />
          <div 
            className="w-6 h-6 rounded-sm border"
            style={{ backgroundColor: lightColors.card }}
            title="Card"
          />
          <div 
            className="w-6 h-6 rounded-sm border"
            style={{ backgroundColor: lightColors.background }}
            title="Background"
          />
          <div 
            className="w-6 h-6 rounded-sm border"
            style={{ backgroundColor: lightColors.muted }}
            title="Muted"
          />
        </div>
        
        {/* Font Preview */}
        <div className="text-xs text-muted-foreground">
          <div>폰트: {theme.cssVars.theme['font-sans'].split(',')[0]}</div>
          <div>반지름: {theme.cssVars.theme.radius}</div>
        </div>
      </CardContent>
    </Card>
  );
};

interface ThemeSelectorProps {
  className?: string;
}

const ThemeSelector: React.FC<ThemeSelectorProps> = ({ className = "" }) => {
  const { 
    currentTheme, 
    isDarkMode, 
    changeTheme, 
    toggleDarkMode, 
    getAvailableThemes 
  } = useTheme();

  const availableThemes = getAvailableThemes();

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          테마 설정
          <div className="flex items-center gap-2">
            <Button
              variant={isDarkMode ? "default" : "outline"}
              size="sm"
              onClick={toggleDarkMode}
              className="text-xs"
            >
              {isDarkMode ? '🌙 다크' : '☀️ 라이트'}
            </Button>
          </div>
        </CardTitle>
        <p className="text-sm text-muted-foreground">
          원하는 디자인 테마를 선택하세요. 테마는 자동으로 저장됩니다.
        </p>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {availableThemes.map((theme) => (
            <ThemePreview
              key={theme.id}
              themeName={theme.id}
              isActive={currentTheme === theme.id}
              onClick={() => changeTheme(theme.id)}
            />
          ))}
        </div>
        
        <div className="mt-6 p-4 bg-muted rounded-lg">
          <div className="text-sm font-medium mb-2">현재 테마 정보</div>
          <div className="text-xs text-muted-foreground space-y-1">
            <div>선택된 테마: <span className="font-medium">{availableThemes.find(t => t.id === currentTheme)?.displayName}</span></div>
            <div>다크 모드: <span className="font-medium">{isDarkMode ? '활성' : '비활성'}</span></div>
            <div>저장 위치: 브라우저 로컬 스토리지</div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default ThemeSelector;